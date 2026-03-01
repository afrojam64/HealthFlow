package com.healthflow.service;

import com.healthflow.domain.*;
import com.healthflow.repo.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class SchedulingService {
  private final AvailabilityBaseRepository availabilityRepo;
  private final AgendaExceptionRepository exceptionRepo;
  private final AppointmentRepository appointmentRepo;
  private final PatientRepository patientRepo;
  private final NotificationService notificationService;

  private final ZoneId zoneId;
  private final int slotMinutes;
  private final int minLeadMinutes;

public SchedulingService(
        AvailabilityBaseRepository availabilityRepo,
        AgendaExceptionRepository exceptionRepo,
        AppointmentRepository appointmentRepo,
        PatientRepository patientRepo, NotificationService notificationService,
        @Value("${healthflow.timezone:America/Bogota}") String tz,
        @Value("${healthflow.appointment.slotMinutes:30}") int slotMinutes,
        @Value("${healthflow.appointment.minLeadMinutes:120}") int minLeadMinutes
) {
    this.availabilityRepo = availabilityRepo;
    this.exceptionRepo = exceptionRepo;
    this.appointmentRepo = appointmentRepo;
    this.patientRepo = patientRepo;
    this.notificationService = notificationService;
    this.zoneId = ZoneId.of(tz);
    this.slotMinutes = slotMinutes;
    this.minLeadMinutes = minLeadMinutes;
}

  /**
   * HU03: Calcula slots libres en un día.
   * Cruza: disponibilidad_base vs excepciones_agenda vs citas.
   */
public List<OffsetDateTime> getFreeSlots(UUID professionalId, LocalDate date) {
    int dayOfWeekDoc = toDocDayOfWeek(date.getDayOfWeek()); // 0..6

    // 1. Obtener horario base y excepciones en una sola query cada uno (ya están optimizados)
    List<AvailabilityBase> bases = availabilityRepo.findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeekDoc);
    if (bases.isEmpty()) {
        return List.of(); // No hay horario base para este día
    }

    List<AgendaException> exs = exceptionRepo.findByProfessionalIdAndDate(professionalId, date);

    // 2. Verificar si hay bloqueo todo el día
    boolean blockedAllDay = exs.stream()
            .anyMatch(e -> e.getType() == ExceptionType.BLOQUEO
                    && e.getStartTime() == null
                    && e.getEndTime() == null);
    if (blockedAllDay) return List.of();

    // 3. Construir ventanas de atención (base + extras)
    List<TimeWindow> windows = new ArrayList<>();
    for (AvailabilityBase b : bases) {
        windows.add(new TimeWindow(b.getStartTime(), b.getEndTime()));
    }
    for (AgendaException e : exs) {
        if (e.getType() == ExceptionType.EXTRA && e.getStartTime() != null) {
            windows.add(new TimeWindow(e.getStartTime(), e.getEndTime()));
        }
    }
    windows = mergeWindows(windows);

    // 4. Construir bloqueos por rango
    List<TimeWindow> blocks = new ArrayList<>();
    for (AgendaException e : exs) {
        if (e.getType() == ExceptionType.BLOQUEO && e.getStartTime() != null) {
            blocks.add(new TimeWindow(e.getStartTime(), e.getEndTime()));
        }
    }

    // 5. Obtener citas activas (no canceladas) - AHORA OPTIMIZADO
    OffsetDateTime startOfDay = date.atStartOfDay(zoneId).toOffsetDateTime();
    OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
    List<Appointment> appts = appointmentRepo.findActiveByProfessionalIdAndDateTimeBetween(
            professionalId, startOfDay, endOfDay
    );

    Set<OffsetDateTime> reserved = new HashSet<>();
    for (Appointment a : appts) {
        reserved.add(a.getDateTime().withOffsetSameInstant(startOfDay.getOffset()));
    }

    // 6. Regla: mínimo tiempo de anticipación
    ZonedDateTime now = ZonedDateTime.now(zoneId);
    ZonedDateTime minAllowed = now.plusMinutes(minLeadMinutes);

    // 7. Generar slots
    List<OffsetDateTime> free = new ArrayList<>();
    for (TimeWindow w : windows) {
        LocalTime t = w.start;
        while (!t.plusMinutes(slotMinutes).isAfter(w.end)) {
            LocalTime slotEnd = t.plusMinutes(slotMinutes);

            if (!isBlocked(t, slotEnd, blocks)) {
                OffsetDateTime slot = date.atTime(t).atZone(zoneId).toOffsetDateTime();
                ZonedDateTime slotZdt = slot.atZoneSameInstant(zoneId);

                if (!slotZdt.isBefore(minAllowed)) {
                    OffsetDateTime slotNormalized = slot.withOffsetSameInstant(startOfDay.getOffset());
                    if (!reserved.contains(slotNormalized)) {
                        free.add(slot);
                    }
                }
            }
            t = t.plusMinutes(slotMinutes);
        }
    }

    free.sort(Comparator.naturalOrder());
    return free;
}


@Transactional
public Appointment book(UUID professionalId, Patient patientPayload, OffsetDateTime dateTime) {
    // 0) Validación básica
    if (dateTime == null) {
        throw new DomainException("dateTime es obligatorio");
    }

    // 1) Regla: no pasado + mínimo tiempo de anticipación
    ZonedDateTime now = ZonedDateTime.now(zoneId);
    ZonedDateTime minAllowed = now.plusMinutes(minLeadMinutes);
    ZonedDateTime requested = dateTime.atZoneSameInstant(zoneId);

    if (requested.isBefore(minAllowed)) {
        throw new DomainException("No puedes agendar en el pasado o con menos de "
                + minLeadMinutes + " minutos de anticipación.");
    }

    // 2) Regla: el slot debe existir (estar dentro de disponibilidad/extra y no bloqueado)
    LocalDate date = requested.toLocalDate();
    LocalTime time = requested.toLocalTime().withSecond(0).withNano(0);

    // a) Debe estar alineado al tamaño del slot (ej. 30 min)
    if ((time.getMinute() % slotMinutes) != 0) {
        throw new DomainException("Hora inválida: debe caer en intervalos de " + slotMinutes + " minutos.");
    }

    // b) Debe aparecer en los slots libres calculados para ese día
    List<OffsetDateTime> freeSlots = getFreeSlots(professionalId, date);
    boolean isFree = freeSlots.stream().anyMatch(s ->
            s.atZoneSameInstant(zoneId).toLocalTime().withSecond(0).withNano(0).equals(time)
    );

    if (!isFree) {
        throw new DomainException("Ese horario ya no está disponible. Por favor elige otro.");
    }

    // 3) Upsert paciente por documento (HU03)
    Patient patient = patientRepo.findByDocNumber(patientPayload.getDocNumber())
            .map(existing -> {
                existing.setEmail(patientPayload.getEmail());
                existing.setPhone(patientPayload.getPhone());
                // Si quieres también podrías actualizar nombres/apellidos aquí
                return existing;
            })
            .orElse(patientPayload);

    patient = patientRepo.save(patient);

    // 4) Crear cita (la concurrencia final la asegura el UNIQUE en DB)
    Appointment appt = new Appointment();
    appt.setProfessionalId(professionalId);
    appt.setPatientId(patient.getId());
    appt.setDateTime(dateTime);

    Appointment saved = appointmentRepo.save(appt);

// ENVÍO MOCK (solo imprime en consola)
    notificationService.sendBookingEmail(patient.getEmail(), saved);

    return saved;
}


private boolean isBlocked(LocalTime start, LocalTime end, List<TimeWindow> blocks) {
for (TimeWindow b : blocks) {
  if (start.isBefore(b.end) && b.start.isBefore(end)) return true;
}
    return false;
}

  // Convierte java.time.DayOfWeek (MON=1..SUN=7) a doc (0=Dom..6=Sab)
  private int toDocDayOfWeek(DayOfWeek dow) {
    return switch (dow) {
      case SUNDAY -> 0;
      case MONDAY -> 1;
      case TUESDAY -> 2;
      case WEDNESDAY -> 3;
      case THURSDAY -> 4;
      case FRIDAY -> 5;
      case SATURDAY -> 6;
    };
  }

  private static List<TimeWindow> mergeWindows(List<TimeWindow> windows) {
    if (windows.isEmpty()) return windows;
    windows.sort(Comparator.comparing(w -> w.start));
    List<TimeWindow> merged = new ArrayList<>();
    TimeWindow cur = windows.get(0);
    for (int i=1;i<windows.size();i++) {
      TimeWindow nxt = windows.get(i);
      if (!nxt.start.isAfter(cur.end)) {
        cur = new TimeWindow(cur.start, cur.end.isAfter(nxt.end) ? cur.end : nxt.end);
      } else {
        merged.add(cur);
        cur = nxt;
      }
    }
    merged.add(cur);
    return merged;
  }

  private record TimeWindow(LocalTime start, LocalTime end) {}


    public void validateRescheduleOrThrow(UUID professionalId, UUID appointmentId, OffsetDateTime newDateTime) {
        if (newDateTime == null) {
            throw new DomainException("newDateTime es obligatorio");
        }

        // Regla: no pasado + mínimo tiempo de anticipación
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime minAllowed = now.plusMinutes(minLeadMinutes);
        ZonedDateTime requested = newDateTime.atZoneSameInstant(zoneId);

        if (requested.isBefore(minAllowed)) {
            throw new DomainException("No puedes reprogramar en el pasado o con menos de "
                    + minLeadMinutes + " minutos de anticipación.");
        }

        LocalDate date = requested.toLocalDate();
        LocalTime time = requested.toLocalTime().withSecond(0).withNano(0);

        // Debe estar alineado al tamaño del slot (ej. 30 min)
        if ((time.getMinute() % slotMinutes) != 0) {
            throw new DomainException("Hora inválida: debe caer en intervalos de " + slotMinutes + " minutos.");
        }

        // Debe estar dentro de los slots libres
        // Importante: si estás reprogramando a la misma hora que ya tenía esa misma cita,
        // el slot no aparecerá como libre si tu cita sigue "reservándolo".
        // Por eso: permitimos el mismo horario si coincide con la cita actual.
        var existing = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada para reprogramación."));

        ZonedDateTime existingZdt = existing.getDateTime().atZoneSameInstant(zoneId);
        boolean sameSlotAsCurrent =
                existingZdt.toLocalDate().equals(date) &&
                        existingZdt.toLocalTime().withSecond(0).withNano(0).equals(time);

        if (sameSlotAsCurrent) return;

        List<OffsetDateTime> freeSlots = getFreeSlots(professionalId, date);
        boolean isFree = freeSlots.stream().anyMatch(s ->
                s.atZoneSameInstant(zoneId).toLocalTime().withSecond(0).withNano(0).equals(time)
        );

        if (!isFree) {
            throw new DomainException("Ese horario ya no está disponible. Por favor elige otro.");
        }
    }
}
