package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.Patient;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/public/booking")
public class PublicAppointmentPageController {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final String publicBaseUrl;
    private final String timezone;

    public PublicAppointmentPageController(AppointmentRepository appointmentRepository,
                                           NotificationService notificationService,
                                           @Value("${healthflow.publicBaseUrl:http://localhost:8080}") String publicBaseUrl,
                                           @Value("${healthflow.timezone:America/Bogota}") String timezone) {
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.publicBaseUrl = publicBaseUrl;
        this.timezone = timezone;
    }

    @Transactional
    @GetMapping("/confirm")
    public String confirmPage(@RequestParam("token") UUID token, Model model) {

        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        // Forzar carga de paciente y profesional para evitar LazyInitializationException
        appt.getPatient().getFirstName(); // carga el paciente
        appt.getProfessional().getFullName(); // carga el profesional (no se usa, pero por si acaso)

        if (appt.getStatus() == AppointmentStatus.CANCELADA) {
            model.addAttribute("title", "No se puede confirmar");
            model.addAttribute("message", "Esta cita fue cancelada y no se puede confirmar.");
            model.addAttribute("ok", false);
        } else {
            appt.setStatus(AppointmentStatus.CONFIRMADA);
            Appointment saved = appointmentRepository.save(appt);

            Patient patient = saved.getPatient();
            notificationService.sendStatusEmail(patient.getEmail(), saved);

            model.addAttribute("title", "¡Cita confirmada!");
            model.addAttribute("message", "Gracias. Tu cita quedó confirmada.");
            model.addAttribute("ok", true);
            appt = saved;
        }

        model.addAttribute("appointment", appt);
        model.addAttribute("token", token.toString());
        model.addAttribute("statusUrl", publicBaseUrl + "/public/booking/status?token=" + token);
        model.addAttribute("icsUrl", publicBaseUrl + "/api/public/appointments/" + token + "/calendar.ics");
        model.addAttribute("cancelUrl", publicBaseUrl + "/public/booking/cancel?token=" + token);

        return "public/public-appointment-confirm";
    }

    @Transactional
    @GetMapping("/cancel")
    public String cancelPage(@RequestParam("token") UUID token, Model model) {

        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        // Forzar carga
        appt.getPatient().getFirstName();

        if (appt.getStatus() == AppointmentStatus.ATENDIDA) {
            model.addAttribute("title", "No se puede cancelar");
            model.addAttribute("message", "Esta cita ya fue atendida y no se puede cancelar.");
            model.addAttribute("ok", false);
        } else {
            appt.setStatus(AppointmentStatus.CANCELADA);
            Appointment saved = appointmentRepository.save(appt);

            Patient patient = saved.getPatient();
            notificationService.sendStatusEmail(patient.getEmail(), saved);

            model.addAttribute("title", "Cita cancelada");
            model.addAttribute("message", "Tu cita fue cancelada. Si quieres, agenda un nuevo horario.");
            model.addAttribute("ok", true);
            appt = saved;
        }

        model.addAttribute("appointment", appt);
        model.addAttribute("token", token.toString());
        model.addAttribute("statusUrl", publicBaseUrl + "/public/booking/status?token=" + token);
        model.addAttribute("icsUrl", publicBaseUrl + "/api/public/appointments/" + token + "/calendar.ics");

        return "public/public-appointment-cancel";
    }

    @Transactional
    @GetMapping("/status")
    public String statusPage(@RequestParam("token") UUID token, Model model) {

        Appointment appt = appointmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new DomainException("No encontramos tu cita. Verifica el enlace."));

        // Forzar carga
        appt.getPatient().getFirstName();

        ZoneId zoneId = ZoneId.of(this.timezone);
        ZonedDateTime local = appt.getDateTime().atZoneSameInstant(zoneId);
        model.addAttribute("dateTimeLocal", local);

        model.addAttribute("title", "Estado de tu cita");
        model.addAttribute("appointment", appt);
        model.addAttribute("token", token.toString());

        model.addAttribute("confirmUrl", publicBaseUrl + "/public/booking/confirm?token=" + token);
        model.addAttribute("cancelUrl", publicBaseUrl + "/public/booking/cancel?token=" + token);
        model.addAttribute("icsUrl", publicBaseUrl + "/api/public/appointments/" + token + "/calendar.ics");

        return "public/public-appointment-status";
    }
}