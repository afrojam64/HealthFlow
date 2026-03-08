package com.healthflow.web;

import com.healthflow.domain.AppointmentStatus;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

@Controller
public class DashboardController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final ZoneId zoneId;

    public DashboardController(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            ProfessionalRepository professionalRepository,
            @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.zoneId = ZoneId.of(tz);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Obtener estadísticas reales
        DashboardStats stats = getStats();

        // Formatear fecha actual
        LocalDate today = LocalDate.now(zoneId);
        String fechaFormateada = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " de " +
                today.getYear();

        model.addAttribute("title", "Dashboard - HealthFlow");
        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("fechaActual", fechaFormateada);

        return "fragments/layout";
    }

    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public DashboardStats getStats() {
        LocalDate today = LocalDate.now(zoneId);
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        // Para Appointment usamos OffsetDateTime
        OffsetDateTime startOfDay = today.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = today.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime startOfWeek = weekAgo.atStartOfDay(zoneId).toOffsetDateTime();

        // CORREGIDO: Para Patient también usamos OffsetDateTime
        OffsetDateTime startOfMonth = monthAgo.atStartOfDay(zoneId).toOffsetDateTime();

        // Usar los métodos con los tipos correctos
        long citasHoy = appointmentRepository.countByFechaHoraBetween(startOfDay, endOfDay);
        long citasSemana = appointmentRepository.countByFechaHoraAfter(startOfWeek);
        long pacientesNuevos = patientRepository.countByCreatedAtAfter(startOfMonth);
        long totalProfesionales = professionalRepository.count();

        // Usar AppointmentStatus.ATENDIDA en lugar de String
        long citasAtendidas = appointmentRepository.countByEstado(AppointmentStatus.ATENDIDA);

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
