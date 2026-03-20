package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.Professional;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Controller
public class DashboardController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final ZoneId zoneId;

    public DashboardController(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            ProfessionalRepository professionalRepository,
            UserRepository userRepository,
            @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.zoneId = ZoneId.of(tz);
    }

    private UUID getCurrentProfessionalId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
        return professional.getId();
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> date, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDate viewDate = date.orElse(LocalDate.now(zoneId));
        DashboardStats stats = getStats();
        LocalDate today = LocalDate.now(zoneId);
        String fechaFormateadaHoy = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " de " +
                today.getYear();
        String fechaTablaCitas = viewDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                viewDate.getDayOfMonth() + " de " +
                viewDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        OffsetDateTime startOfDay = viewDate.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = viewDate.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        UUID professionalId = getCurrentProfessionalId();
        List<Appointment> proximasCitas = appointmentRepository
                .findActiveByProfessionalIdAndDateTimeBetween(professionalId, startOfDay, endOfDay);

        model.addAttribute("title", "Dashboard - HealthFlow");
        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("fechaActual", fechaFormateadaHoy);
        model.addAttribute("fechaTablaCitas", fechaTablaCitas);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("prevDate", viewDate.minusDays(1));
        model.addAttribute("nextDate", viewDate.plusDays(1));
        model.addAttribute("today", today);
        model.addAttribute("contenido", "dashboard/content");

        return "fragments/layout";
    }

    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public DashboardStats getStats() {
        LocalDate today = LocalDate.now(zoneId);
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        OffsetDateTime startOfDay = today.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = today.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime startOfWeek = weekAgo.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime startOfMonth = monthAgo.atStartOfDay(zoneId).toOffsetDateTime();

        long citasHoy = appointmentRepository.countByDateTimeBetween(startOfDay, endOfDay);
        long citasSemana = appointmentRepository.countByDateTimeAfter(startOfWeek);
        long pacientesNuevos = patientRepository.countByCreatedAtAfter(startOfMonth);
        long totalProfesionales = professionalRepository.count();
        long citasAtendidas = appointmentRepository.countByStatus(AppointmentStatus.ATENDIDA);

        int ocupacion = totalProfesionales > 0 ?
                (int) ((citasAtendidas * 100) / (totalProfesionales * 10)) : 0;

        return new DashboardStats(
                citasHoy,
                citasSemana,
                pacientesNuevos,
                ocupacion
        );
    }

    public record DashboardStats(
            long citasHoy,
            long citasSemana,
            long pacientesNuevos,
            int ocupacion
    ) {}
}