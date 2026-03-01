package com.healthflow.web;

import com.healthflow.repo.AppointmentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Controller
public class DashboardController {

    private final AppointmentRepository appointmentRepository;
    private final ZoneId zoneId;

    public DashboardController(
            AppointmentRepository appointmentRepository,
            @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.zoneId = ZoneId.of(tz);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtener el usuario actual
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("username", username);

        // Obtener citas de hoy (esto lo mejoraremos cuando tengamos el profesional asociado al usuario)
        // Por ahora, mostramos un placeholder
        model.addAttribute("citasHoy", 0);

        return "dashboard";
    }

    // Este endpoint será útil cuando tengamos la relación User-Profesional
    @GetMapping("/api/dashboard/stats")
    @org.springframework.web.bind.annotation.ResponseBody
    public DashboardStats getStats() {
        // Por ahora, retornamos estadísticas de ejemplo
        // TODO: Implementar cuando tengamos la relación User-Profesional

        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime startOfDay = today.atStartOfDay(zoneId);
        ZonedDateTime endOfDay = today.plusDays(1).atStartOfDay(zoneId);

        return new DashboardStats(
                5,  // citas hoy (hardcoded por ahora)
                12, // citas semana
                3,  // pacientes nuevos
                85  // ocupación %
        );
    }

    public record DashboardStats(long citasHoy, long citasSemana, long pacientesNuevos, int ocupacion) {}
}