package com.healthflow.api;

import com.healthflow.api.dto.RescheduleRequest;
import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.Patient;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.NotificationService;
import com.healthflow.service.SchedulingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/appointments")
public class PublicAppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final SchedulingService schedulingService;

    public PublicAppointmentController(AppointmentRepository appointmentRepository,
                                       NotificationService notificationService,
                                       SchedulingService schedulingService) {
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.schedulingService = schedulingService;
    }

    @PostMapping("/{token}/confirm")
    @ResponseStatus(HttpStatus.OK)
    public Appointment confirm(@PathVariable("token") UUID token) {
        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        if (appt.getStatus() == AppointmentStatus.CANCELADA) {
            throw new DomainException("Esta cita fue cancelada y no se puede confirmar.");
        }

        appt.setStatus(AppointmentStatus.CONFIRMADA);
        Appointment saved = appointmentRepository.save(appt);

        Patient patient = saved.getPatient(); // <-- CORREGIDO

        notificationService.sendStatusEmail(patient.getEmail(), saved);

        return saved;
    }

    @PostMapping("/{token}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public Appointment cancel(@PathVariable("token") UUID token) {
        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        if (appt.getStatus() == AppointmentStatus.ATENDIDA) {
            throw new DomainException("Esta cita ya fue atendida y no se puede cancelar.");
        }

        appt.setStatus(AppointmentStatus.CANCELADA);
        Appointment saved = appointmentRepository.save(appt);

        Patient patient = saved.getPatient(); // <-- CORREGIDO

        notificationService.sendStatusEmail(patient.getEmail(), saved);

        return saved;
    }

    // ✅ NUEVO: Reprogramar por token
    @PostMapping("/{token}/reschedule")
    @ResponseStatus(HttpStatus.OK)
    public Appointment reschedule(@PathVariable("token") UUID token,
                                  @Valid @RequestBody RescheduleRequest req) {

        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        // Reglas de estado
        if (appt.getStatus() == AppointmentStatus.CANCELADA) {
            throw new DomainException("Esta cita fue cancelada y no se puede reprogramar.");
        }
        if (appt.getStatus() == AppointmentStatus.ATENDIDA) {
            throw new DomainException("Esta cita ya fue atendida y no se puede reprogramar.");
        }

        OffsetDateTime newDateTime = req.newDateTime();

        // Validar que el nuevo horario sea válido y esté disponible usando la misma lógica del agendamiento
        // Esto aplica: no pasado, minLeadMinutes, bloqueos/extras y reservas existentes
        schedulingService.validateRescheduleOrThrow(appt.getProfessional().getId(), appt.getId(), newDateTime); // <-- CORREGIDO

        // Aplicar cambio
        appt.setDateTime(newDateTime);

        // ✅ IMPORTANTE: resetear idempotencia de recordatorio
        appt.setReminderSentAt(null);

        Appointment saved = appointmentRepository.save(appt);

        Patient patient = saved.getPatient(); // <-- CORREGIDO

        notificationService.sendRescheduleEmail(patient.getEmail(), saved);

        return saved;
    }

    @GetMapping("/{token}")
    public AppointmentStatusResponse status(@PathVariable("token") UUID token) {
        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        return new AppointmentStatusResponse(appt.getStatus().name(), appt.getDateTime(), appt.getProfessional().getId()); // <-- CORREGIDO
    }

    public record AppointmentStatusResponse(String status, OffsetDateTime dateTime, UUID professionalId) {}
}
