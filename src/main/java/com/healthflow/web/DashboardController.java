package com.healthflow.web;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import com.healthflow.service.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
public class DashboardController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final WeeklyAvailabilityRepository weeklyAvailabilityRepository;
    private final ZoneId zoneId;

    public DashboardController(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            ProfessionalRepository professionalRepository,
            UserRepository userRepository,
            WeeklyAvailabilityRepository weeklyAvailabilityRepository,
            @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.weeklyAvailabilityRepository = weeklyAvailabilityRepository;
        this.zoneId = ZoneId.of(tz);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no autenticado"));
        Professional professional = professionalRepository.findByUser(user)
                .orElseThrow(() -> new DomainException("Profesional no encontrado para el usuario"));

        DashboardStats stats = getStats();
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.PENDIENTE, AppointmentStatus.CONFIRMADA);
        List<Appointment> proximasCitas = appointmentRepository.findByProfessionalAndStatusInAndDateTimeAfterOrderByDateTimeAsc(
                professional, statuses, OffsetDateTime.now(zoneId)
        );
        LocalDate startOfCurrentWeek = LocalDate.now(zoneId).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        List<WeeklyAvailability> agendasConfiguradas = weeklyAvailabilityRepository.findByProfessionalAndWeekStartDateGreaterThanEqualOrderByWeekStartDateAsc(
                professional, startOfCurrentWeek
        );

        model.addAttribute("title", "Dashboard - HealthFlow");
        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("agendasConfiguradas", agendasConfiguradas);
        model.addAttribute("currentPage", "dashboard");

        // CORRECTO: Le decimos al layout que el archivo de contenido es 'dashboard'
        model.addAttribute("content", "dashboard");
        return "fragments/layout"; // Y que renderice el layout principal
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

        long citasHoy = appointmentRepository.countByFechaHoraBetween(startOfDay, endOfDay);
        long citasSemana = appointmentRepository.countByFechaHoraAfter(startOfWeek);
        long pacientesNuevos = patientRepository.countByCreatedAtAfter(startOfMonth);
        long totalProfesionales = professionalRepository.count();
        long citasAtendidas = appointmentRepository.countByEstado(AppointmentStatus.ATENDIDA);

        int ocupacion = totalProfesionales > 0 ?
                (int) ((citasAtendidas * 100) / (totalProfesionales * 10)) : 0;

        return new DashboardStats(citasHoy, citasSemana, pacientesNuevos, ocupacion);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    public record DashboardStats(long citasHoy, long citasSemana, long pacientesNuevos, int ocupacion) {}
}