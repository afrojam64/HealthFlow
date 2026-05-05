package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.repo.RipsGenerationRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.PermisoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
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
    private final RipsGenerationRepository ripsGenerationRepository;
    private final PermisoService permisoService;
    private final ZoneId zoneId;

    public DashboardController(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            ProfessionalRepository professionalRepository,
            UserRepository userRepository,
            RipsGenerationRepository ripsGenerationRepository,
            PermisoService permisoService,
            @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.ripsGenerationRepository = ripsGenerationRepository;
        this.permisoService = permisoService;
        this.zoneId = ZoneId.of(tz);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
    }

    private UUID getCurrentProfessionalId() {
        User user = getCurrentUser();
        if ("ASISTENTE".equals(user.getRole())) {
            UUID medicoId = permisoService.getMedicoIdByAsistente(user.getId());
            if (medicoId == null) {
                throw new AccessDeniedException("No tienes un médico asociado");
            }
            return medicoId;
        } else {
            Professional professional = professionalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
            return professional.getId();
        }
    }

    private boolean hayPeriodoPendiente(UUID professionalId) {
        LocalDate hoy = LocalDate.now(zoneId);
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

        boolean ripsPendiente = hayPeriodoPendiente(professionalId);

        model.addAttribute("title", "Dashboard - HealthFlow");
        model.addAttribute("username", username);
        model.addAttribute("fechaActual", fechaFormateadaHoy);
        model.addAttribute("fechaTablaCitas", fechaTablaCitas);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("prevDate", viewDate.minusDays(1));
        model.addAttribute("nextDate", viewDate.plusDays(1));
        model.addAttribute("today", today);
        model.addAttribute("ripsPendiente", ripsPendiente);
        model.addAttribute("contenido", "dashboard/content");

        return "fragments/layout";
    }

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

        UUID professionalId = getCurrentProfessionalId();

        // Cantidad de citas en el período (sin importar estado)
        long citasEnPeriodo = appointmentRepository.countByProfessionalIdAndDateTimeBetween(professionalId, startOfDay, endOfDay);

        // Pacientes nuevos (primera cita con este profesional en el período)
        long pacientesNuevos = countNewPatients(professionalId, startOfDay, endOfDay);

        // Citas atendidas en el período (status ATENDIDA)
        long citasAtendidas = countAttendedAppointments(professionalId, startOfDay, endOfDay);

        int ocupacion = citasEnPeriodo > 0 ? (int) ((citasAtendidas * 100) / citasEnPeriodo) : 0;
        int tasaAsistencia = ocupacion;

        return new DashboardStats(citasEnPeriodo, pacientesNuevos, ocupacion, tasaAsistencia);
    }

    private long countNewPatients(UUID professionalId, OffsetDateTime start, OffsetDateTime end) {
        // Obtener todos los pacientes que tuvieron su primera cita con el profesional en el rango
        List<Object[]> firstAppointments = appointmentRepository.findFirstAppointmentDatePerPatient(professionalId, start, end);
        // firstAppointments contiene [patientId, firstAppointmentDate]
        // Solo contamos aquellos donde la primera cita está dentro del rango (ya está filtrado por la consulta)
        return firstAppointments.size();
    }

    private long countAttendedAppointments(UUID professionalId, OffsetDateTime start, OffsetDateTime end) {
        // Usar el método existente que devuelve lista y luego contar
        List<Appointment> attended = appointmentRepository.findByProfessionalIdAndDateTimeBetweenAndStatus(
                professionalId, start, end, AppointmentStatus.ATENDIDA);
        return attended.size();
    }

    @GetMapping("/api/dashboard/citas-por-dia")
    @ResponseBody
    public Map<String, Object> getCitasPorDia(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        OffsetDateTime startOfDay = start.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = end.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        UUID professionalId = getCurrentProfessionalId();

        // Obtener citas del profesional en el rango usando método existente
        List<Appointment> citas = appointmentRepository.findByProfessional_IdAndDateTimeBetween(professionalId, startOfDay, endOfDay);
        // Ordenar por fecha (aunque la consulta podría no ordenar, lo hacemos en Java)
        citas.sort(Comparator.comparing(Appointment::getDateTime));

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