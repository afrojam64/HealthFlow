package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.repo.RipsGenerationRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final RipsGenerationRepository ripsGenerationRepository;  // ← nuevo
    private final ZoneId zoneId;

    public DashboardController(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            ProfessionalRepository professionalRepository,
            UserRepository userRepository,
            RipsGenerationRepository ripsGenerationRepository,  // ← nuevo
            @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.ripsGenerationRepository = ripsGenerationRepository;  // ← nuevo
        this.zoneId = ZoneId.of(tz);
    }

    private UUID getCurrentProfessionalId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        var professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
        return professional.getId();
    }

    /**
     * Verifica si el profesional tiene al menos un período pendiente de reporte RIPS.
     * Un período pendiente = mes anterior al actual en el que no hay ninguna generación.
     */
    private boolean hayPeriodoPendiente(UUID professionalId) {
        LocalDate hoy = LocalDate.now(zoneId);
        // Período: mes anterior (por ejemplo, si hoy es 26/04/2026, período es 01/03/2026 - 31/03/2026)
        LocalDate fechaDesde = hoy.minusMonths(1).withDayOfMonth(1);
        LocalDate fechaHasta = fechaDesde.withDayOfMonth(fechaDesde.lengthOfMonth());

        long generacionesExistentes = ripsGenerationRepository.countByProfessionalIdAndFechaDesdeAndFechaHasta(
                professionalId, fechaDesde, fechaHasta);
        return generacionesExistentes == 0;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> date, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDate viewDate = date.orElse(LocalDate.now(zoneId));
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

        // Verificar RIPS pendiente
        boolean ripsPendiente = hayPeriodoPendiente(professionalId);

        model.addAttribute("title", "Dashboard - HealthFlow");
        model.addAttribute("username", username);
        model.addAttribute("fechaActual", fechaFormateadaHoy);
        model.addAttribute("fechaTablaCitas", fechaTablaCitas);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("prevDate", viewDate.minusDays(1));
        model.addAttribute("nextDate", viewDate.plusDays(1));
        model.addAttribute("today", today);
        model.addAttribute("ripsPendiente", ripsPendiente);  // ← nuevo
        model.addAttribute("contenido", "dashboard/content");

        return "fragments/layout";
    }

    // Resto de métodos (getStats, getCitasPorDia) no se modifican
    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public DashboardStats getStats(
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        LocalDate today = LocalDate.now(zoneId);
        LocalDate startDate = start != null ? start : today;
        LocalDate endDate = end != null ? end : today;

        OffsetDateTime startOfDay = startDate.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = endDate.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        long citasEnPeriodo = appointmentRepository.countByDateTimeBetween(startOfDay, endOfDay);
        long pacientesNuevos = patientRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        long citasAtendidas = appointmentRepository.countByStatusAndDateTimeBetween(AppointmentStatus.ATENDIDA, startOfDay, endOfDay);
        int ocupacion = citasEnPeriodo > 0 ? (int) ((citasAtendidas * 100) / citasEnPeriodo) : 0;
        int tasaAsistencia = ocupacion;

        return new DashboardStats(citasEnPeriodo, pacientesNuevos, ocupacion, tasaAsistencia);
    }

    @GetMapping("/api/dashboard/citas-por-dia")
    @ResponseBody
    public Map<String, Object> getCitasPorDia(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        OffsetDateTime startOfDay = start.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = end.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        List<Appointment> citas = appointmentRepository.findByDateTimeBetweenOrderByDateTimeAsc(startOfDay, endOfDay);

        Map<LocalDate, Long> citasPorDia = citas.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDateTime().atZoneSameInstant(zoneId).toLocalDate(),
                        Collectors.counting()
                ));

        List<String> fechas = new ArrayList<>();
        List<Long> conteos = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            fechas.add(date.toString());
            conteos.add(citasPorDia.getOrDefault(date, 0L));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("fechas", fechas);
        response.put("conteos", conteos);
        return response;
    }

    public record DashboardStats(
            long citasEnPeriodo,
            long pacientesNuevos,
            int ocupacion,
            int tasaAsistencia
    ) {}
}