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
    private final AvailabilityBaseRepository availabilityBaseRepository;
    private final AgendaExceptionRepository agendaExceptionRepository;

    private final ZoneId zoneId;
    private final int slotMinutes;
    private final int minLeadMinutes;

    public SchedulingService(
            WeeklyAvailabilityRepository weeklyAvailabilityRepository,
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            ProfessionalRepository professionalRepository,
            NotificationService notificationService,
            AvailabilityBaseRepository availabilityBaseRepository,
            AgendaExceptionRepository agendaExceptionRepository,
            @Value("${healthflow.timezone:America/Bogota}") String tz,
            @Value("${healthflow.appointment.slotMinutes:30}") int slotMinutes,
            @Value("${healthflow.appointment.minLeadMinutes:120}") int minLeadMinutes
    ) {
        this.weeklyAvailabilityRepository = weeklyAvailabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.notificationService = notificationService;
        this.availabilityBaseRepository = availabilityBaseRepository;
        this.agendaExceptionRepository = agendaExceptionRepository;
        this.zoneId = ZoneId.of(tz);
        this.slotMinutes = slotMinutes;
        this.minLeadMinutes = minLeadMinutes;
    }

    // Obtiene los horarios libres para un profesional en una fecha determinada
    public List<OffsetDateTime> getFreeSlots(UUID professionalId, LocalDate date) {
        List<AgendaException> exceptions = agendaExceptionRepository.findByProfessional_IdAndDate(professionalId, date);
        if (!exceptions.isEmpty()) {
            AgendaException ex = exceptions.get(0);
            if (ex.getType() == ExceptionType.BLOQUEO) {
                return List.of();
            } else if (ex.getType() == ExceptionType.EXTRA && ex.getStartTime() != null && ex.getEndTime() != null) {
                return generateSlotsForRange(professionalId, date, ex.getStartTime(), ex.getEndTime());
            }
            return List.of();
        }

        LocalDate weekStartDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int dayOfWeek = date.getDayOfWeek().getValue();

        Optional<WeeklyAvailability> weeklyOpt = weeklyAvailabilityRepository
                .findAvailabilityForDay(professionalId, weekStartDate, dayOfWeek);

        List<AvailabilityBase> rangos = new ArrayList<>();

        if (weeklyOpt.isPresent() && weeklyOpt.get().getActive()) {
            WeeklyAvailability wa = weeklyOpt.get();
            if (wa.getStartTime() != null && wa.getEndTime() != null) {
                AvailabilityBase temp = new AvailabilityBase();
                temp.setStartTime(wa.getStartTime());
                temp.setEndTime(wa.getEndTime());
                rangos.add(temp);
            } else {
                return List.of();
            }
        } else {
            rangos = availabilityBaseRepository
                    .findByProfessionalIdAndDayOfWeekOrderByStartTimeAsc(professionalId, dayOfWeek);
            if (rangos.isEmpty()) {
                return List.of();
            }
        }

        OffsetDateTime startOfDay = date.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        Set<LocalTime> reservedSlots = appointmentRepository
                .findActiveByProfessionalIdAndDateTimeBetween(professionalId, startOfDay, endOfDay)
                .stream()
                .map(a -> a.getDateTime().atZoneSameInstant(zoneId).toLocalTime())
                .collect(Collectors.toSet());

        ZonedDateTime minAllowedTime = ZonedDateTime.now(zoneId).plusMinutes(minLeadMinutes);

        List<OffsetDateTime> freeSlots = new ArrayList<>();
        for (AvailabilityBase rango : rangos) {
            LocalTime slotTime = rango.getStartTime();
            while (!slotTime.isAfter(rango.getEndTime())) {
                ZonedDateTime slotZdt = date.atTime(slotTime).atZone(zoneId);
                if (slotZdt.isAfter(minAllowedTime) && !reservedSlots.contains(slotTime)) {
                    freeSlots.add(slotZdt.toOffsetDateTime());
                }
                slotTime = slotTime.plusMinutes(slotMinutes);
            }
        }

        freeSlots.sort(Comparator.naturalOrder());
        return freeSlots;
    }

    // Genera slots para un rango horario específico (usado en excepciones EXTRA)
    private List<OffsetDateTime> generateSlotsForRange(UUID professionalId, LocalDate date, LocalTime start, LocalTime end) {
        OffsetDateTime startOfDay = date.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        Set<LocalTime> reservedSlots = appointmentRepository
                .findActiveByProfessionalIdAndDateTimeBetween(professionalId, startOfDay, endOfDay)
                .stream()
                .map(a -> a.getDateTime().atZoneSameInstant(zoneId).toLocalTime())
                .collect(Collectors.toSet());

        ZonedDateTime minAllowedTime = ZonedDateTime.now(zoneId).plusMinutes(minLeadMinutes);
        List<OffsetDateTime> freeSlots = new ArrayList<>();
        LocalTime slotTime = start;
        while (!slotTime.isAfter(end)) {
            ZonedDateTime slotZdt = date.atTime(slotTime).atZone(zoneId);
            if (slotZdt.isAfter(minAllowedTime) && !reservedSlots.contains(slotTime)) {
                freeSlots.add(slotZdt.toOffsetDateTime());
            }
            slotTime = slotTime.plusMinutes(slotMinutes);
        }
        freeSlots.sort(Comparator.naturalOrder());
        return freeSlots;
    }

    // Agendamiento público (crea o actualiza paciente)
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
        appt.setAccessToken(UUID.randomUUID());

        Appointment saved = this.appointmentRepository.save(appt);
        notificationService.sendBookingEmail(patient.getEmail(), saved);
        return saved;
    }

    // Valida si una cita puede ser reprogramada a un nuevo horario (sin persistir)
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

    // Agendamiento para paciente ya existente (desde portal del paciente) - cita confirmada automáticamente
    @Transactional
    public Appointment bookForPatient(UUID professionalId, UUID patientId, OffsetDateTime dateTime) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new DomainException("Profesional no encontrado"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));

        // Validar fecha/hora usando la misma lógica que en book()
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

        // Crear cita con estado CONFIRMADA
        Appointment appointment = new Appointment();
        appointment.setProfessional(professional);
        appointment.setPatient(patient);
        appointment.setDateTime(dateTime);
        appointment.setStatus(AppointmentStatus.CONFIRMADA);
        appointment.setAccessToken(UUID.randomUUID());

        Appointment saved = appointmentRepository.save(appointment);
        notificationService.sendBookingEmail(patient.getEmail(), saved);
        return saved;
    }

    // Reprogramar cita existente (desde portal del paciente) - se confirma automáticamente
    @Transactional
    public void rescheduleAppointment(UUID appointmentId, OffsetDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));

        if (appointment.getStatus() == AppointmentStatus.ATENDIDA || appointment.getStatus() == AppointmentStatus.CANCELADA) {
            throw new DomainException("No se puede reprogramar una cita atendida o cancelada.");
        }

        // Validar disponibilidad del nuevo horario usando el método existente
        validateRescheduleOrThrow(appointment.getProfessional().getId(), appointmentId, newDateTime);

        // Actualizar cita
        appointment.setDateTime(newDateTime);
        appointment.setStatus(AppointmentStatus.CONFIRMADA);
        appointmentRepository.save(appointment);
        notificationService.sendRescheduleEmail(appointment.getPatient().getEmail(), appointment);
    }
}