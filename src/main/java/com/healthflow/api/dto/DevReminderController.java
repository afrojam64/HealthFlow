package com.healthflow.api.dto;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.Patient;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/dev")
public class DevReminderController {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final ZoneId zoneId;

    public DevReminderController(AppointmentRepository appointmentRepository,
                                 NotificationService notificationService,
                                 @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.zoneId = ZoneId.of(tz);
    }

    // Dispara recordatorios para citas en las próximas N horas (ej: 24)
    @PostMapping("/reminders/run")
    public RunResult run(@RequestParam(name = "hours", defaultValue = "24") int hours) {
        OffsetDateTime now = OffsetDateTime.now(zoneId);
        OffsetDateTime until = now.plusHours(hours);

        int sent = 0;

        for (Appointment appt : appointmentRepository.findAll()) {
            if (appt.getStatus() == AppointmentStatus.CANCELADA) continue;

            // ✅ Idempotencia: si ya se envió, no reenviar
            if (appt.getReminderSentAt() != null) continue;

            OffsetDateTime dt = appt.getDateTime();
            if (!dt.isBefore(now) && !dt.isAfter(until)) {
                Patient patient = appt.getPatient(); // <-- CORREGIDO
                if (patient == null) {
                    throw new DomainException("Paciente no encontrado para esta cita.");
                }

                notificationService.sendReminderEmail(patient.getEmail(), appt);

                // ✅ Marcar como enviado
                appt.setReminderSentAt(OffsetDateTime.now(zoneId));
                appointmentRepository.save(appt);

                sent++;
            }
        }

        return new RunResult(sent, now, until);
    }

    public record RunResult(int sent, OffsetDateTime from, OffsetDateTime to) {}
}
