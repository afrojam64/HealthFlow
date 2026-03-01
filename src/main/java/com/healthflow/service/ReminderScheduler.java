package com.healthflow.service;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;
    private final ZoneId zoneId;

    public ReminderScheduler(AppointmentRepository appointmentRepository,
                             PatientRepository patientRepository,
                             NotificationService notificationService,
                             @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.notificationService = notificationService;
        this.zoneId = ZoneId.of(tz);
    }

    // Todos los días a las 08:00 (hora configurada)
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Bogota")
    @Transactional
    public void runDaily() {
        OffsetDateTime now = OffsetDateTime.now(zoneId);
        OffsetDateTime until = now.plusHours(24);

        // Usar query optimizada en lugar de traer todos los registros
        List<Appointment> appointments = appointmentRepository.findPendingReminders(now, until);

        int sentCount = 0;
        for (Appointment appt : appointments) {
            try {
                var patient = patientRepository.findById(appt.getPatientId())
                        .orElseThrow(() -> new DomainException("Paciente no encontrado para cita: " + appt.getId()));

                notificationService.sendReminderEmail(patient.getEmail(), appt);
                appt.setReminderSentAt(OffsetDateTime.now(zoneId));
                appointmentRepository.save(appt);
                sentCount++;
            } catch (Exception e) {
                // Loggear error pero continuar con las demás citas
                System.err.println("Error enviando recordatorio para cita " + appt.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("Recordatorios enviados: " + sentCount + " de " + appointments.size());
    }
}