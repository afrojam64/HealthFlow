package com.healthflow.service;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulingService {
    private final WeeklyAvailabilityRepository weeklyAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final NotificationService notificationService;

    private final ZoneId zoneId;
    private final int slotMinutes;
    private final int minLeadMinutes;

    public SchedulingService(
            WeeklyAvailabilityRepository weeklyAvailabilityRepository,
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            ProfessionalRepository professionalRepository,
            NotificationService notificationService,
            @Value("${healthflow.timezone:America/Bogota}") String tz,
            @Value("${healthflow.appointment.slotMinutes:30}") int slotMinutes,
            @Value("${healthflow.appointment.minLeadMinutes:120}") int minLeadMinutes
    ) {
        this.weeklyAvailabilityRepository = weeklyAvailabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.notificationService = notificationService;
        this.zoneId = ZoneId.of(tz);
        this.slotMinutes = slotMinutes;
        this.minLeadMinutes = minLeadMinutes;
    }

    public List<OffsetDateTime> getFreeSlots(UUID professionalId, LocalDate date) {
        // 1. Calcular el inicio de la semana y el día de la semana correctos
        LocalDate weekStartDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Lunes, ..., 7=Domingo

        // 2. Buscar la disponibilidad directamente en la BD (lógica simplificada y robusta)
        Optional<WeeklyAvailability> dayAvailabilityOpt = weeklyAvailabilityRepository
                .findAvailabilityForDay(professionalId, weekStartDate, dayOfWeek);

        if (dayAvailabilityOpt.isEmpty()) {
            return List.of();
        }
        WeeklyAvailability availability = dayAvailabilityOpt.get();

        // 3. Obtener citas ya agendadas para ese día
        OffsetDateTime startOfDay = date.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        Set<LocalTime> reservedSlots = appointmentRepository.findActiveByProfessionalIdAndDateTimeBetween(professionalId, startOfDay, endOfDay)
                .stream()
                .map(a -> a.getDateTime().atZoneSameInstant(zoneId).toLocalTime())
                .collect(Collectors.toSet());

        // 4. Regla: mínimo tiempo de anticipación para agendar
        ZonedDateTime minAllowedTime = ZonedDateTime.now(zoneId).plusMinutes(minLeadMinutes);

        // 5. Generar los slots y filtrarlos
        List<OffsetDateTime> freeSlots = new ArrayList<>();
        LocalTime slotTime = availability.getStartTime();
        while (!slotTime.isAfter(availability.getEndTime())) {
            ZonedDateTime slotZdt = date.atTime(slotTime).atZone(zoneId);

            if (slotZdt.isAfter(minAllowedTime) && !reservedSlots.contains(slotTime)) {
                freeSlots.add(slotZdt.toOffsetDateTime());
            }
            slotTime = slotTime.plusMinutes(slotMinutes);
        }

        freeSlots.sort(Comparator.naturalOrder());
        return freeSlots;
    }

    @Transactional
    public Appointment book(UUID professionalId, Patient patientPayload, OffsetDateTime dateTime) {
        if (dateTime == null) {
            throw new DomainException("dateTime es obligatorio");
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime minAllowed = now.plusMinutes(minLeadMinutes);
        ZonedDateTime requested = dateTime.atZoneSameInstant(zoneId);

        if (requested.isBefore(minAllowed)) {
            throw new DomainException("No puedes agendar en el pasado o con menos de " + minLeadMinutes + " minutos de anticipación.");
        }

        LocalDate date = requested.toLocalDate();
        LocalTime time = requested.toLocalTime().withSecond(0).withNano(0);

        if ((time.getMinute() % slotMinutes) != 0) {
            throw new DomainException("Hora inválida: debe caer en intervalos de " + slotMinutes + " minutos.");
        }

        List<OffsetDateTime> freeSlots = getFreeSlots(professionalId, date);
        boolean isFree = freeSlots.stream().anyMatch(s ->
                s.atZoneSameInstant(zoneId).toLocalTime().withSecond(0).withNano(0).equals(time)
        );

        if (!isFree) {
            throw new DomainException("Ese horario ya no está disponible. Por favor elige otro.");
        }

        Patient patient = this.patientRepository.findByDocNumber(patientPayload.getDocNumber())
                .map(existing -> {
                    existing.setEmail(patientPayload.getEmail());
                    existing.setPhone(patientPayload.getPhone());
                    return existing;
                })
                .orElse(patientPayload);

        patient = this.patientRepository.save(patient);

        Professional professional = this.professionalRepository.findById(professionalId)
                .orElseThrow(() -> new DomainException("Profesional no encontrado."));

        Appointment appt = new Appointment();
        appt.setProfessional(professional);
        appt.setPatient(patient);
        appt.setDateTime(dateTime);

        Appointment saved = this.appointmentRepository.save(appt);
        notificationService.sendBookingEmail(patient.getEmail(), saved);
        return saved;
    }

    public void validateRescheduleOrThrow(UUID professionalId, UUID appointmentId, OffsetDateTime newDateTime) {
        if (newDateTime == null) {
            throw new DomainException("newDateTime es obligatorio");
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime minAllowed = now.plusMinutes(minLeadMinutes);
        ZonedDateTime requested = newDateTime.atZoneSameInstant(zoneId);

        if (requested.isBefore(minAllowed)) {
            throw new DomainException("No puedes reprogramar en el pasado o con menos de " + minLeadMinutes + " minutos de anticipación.");
        }

        LocalDate date = requested.toLocalDate();
        LocalTime time = requested.toLocalTime().withSecond(0).withNano(0);

        if ((time.getMinute() % slotMinutes) != 0) {
            throw new DomainException("Hora inválida: debe caer en intervalos de " + slotMinutes + " minutos.");
        }

        var existing = appointmentRepository.findById(appointmentId)
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
