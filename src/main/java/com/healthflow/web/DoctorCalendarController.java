package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.Professional;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorCalendarController {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final ZoneId zoneId;

    public DoctorCalendarController(
            AppointmentRepository appointmentRepository,
            ProfessionalRepository professionalRepository,
            UserRepository userRepository,
            @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.zoneId = ZoneId.of(tz);
    }

    @GetMapping("/calendario")
    public String verCalendario(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            Model model) {

        // Obtener profesional autenticado
        UUID professionalId = getCurrentProfessionalId();

        // Determinar año y mes (si no se proporcionan, usar el actual)
        LocalDate today = LocalDate.now(zoneId);
        int currentYear = year != null ? year : today.getYear();
        int currentMonth = month != null ? month : today.getMonthValue();

        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDate primerDia = yearMonth.atDay(1);
        LocalDate ultimoDia = yearMonth.atEndOfMonth();

        // Rango de fechas en OffsetDateTime
        OffsetDateTime start = primerDia.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime end = ultimoDia.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        // Obtener todas las citas activas del mes
        List<Appointment> citasDelMes = appointmentRepository.findActiveByProfessionalIdAndDateTimeBetween(
                professionalId, start, end);

        // Agrupar citas por día
        Map<LocalDate, List<Appointment>> citasPorDia = citasDelMes.stream()
                .collect(Collectors.groupingBy(
                        cita -> cita.getDateTime().atZoneSameInstant(zoneId).toLocalDate(),
                        Collectors.toList()
                ));

        // Construir estructura para el calendario (semanas)
        List<List<LocalDate>> semanas = new ArrayList<>();
        LocalDate dia = primerDia;
        // Ajustar al lunes anterior si el primer día no es lunes
        int firstDayOfWeekValue = primerDia.getDayOfWeek().getValue(); // 1=lunes, 7=domingo
        if (firstDayOfWeekValue != 1) {
            dia = primerDia.minusDays(firstDayOfWeekValue - 1);
        }

        while (!dia.isAfter(ultimoDia)) {
            List<LocalDate> semana = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                semana.add(dia);
                dia = dia.plusDays(1);
            }
            semanas.add(semana);
        }

        // Nombres de meses para navegación
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        int prevMonth = currentMonth == 1 ? 12 : currentMonth - 1;
        int prevYear = currentMonth == 1 ? currentYear - 1 : currentYear;
        int nextMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        int nextYear = currentMonth == 12 ? currentYear + 1 : currentYear;

        // Variables para el layout (necesarias para que el layout funcione)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String fechaActual = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " de " +
                today.getYear();

        DashboardStats stats = new DashboardStats(0, 0, 0, 0);
        List<Object> proximasCitas = new ArrayList<>();

        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("fechaActual", fechaActual);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("today", today);

        // Datos específicos del calendario
        model.addAttribute("citasPorDia", citasPorDia);
        model.addAttribute("semanas", semanas);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentMonthName", monthNames[currentMonth - 1]);
        model.addAttribute("prevMonth", prevMonth);
        model.addAttribute("prevYear", prevYear);
        model.addAttribute("nextMonth", nextMonth);
        model.addAttribute("nextYear", nextYear);
        model.addAttribute("title", "Calendario de Citas - HealthFlow");
        model.addAttribute("contenido", "doctor/calendario");

        return "fragments/layout";
    }

    private UUID getCurrentProfessionalId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
        return professional.getId();
    }

    // Clase interna para stats (requerida por el layout)
    public static class DashboardStats {
        public final long citasHoy;
        public final long citasSemana;
        public final long pacientesNuevos;
        public final int ocupacion;

        public DashboardStats(long citasHoy, long citasSemana, long pacientesNuevos, int ocupacion) {
            this.citasHoy = citasHoy;
            this.citasSemana = citasSemana;
            this.pacientesNuevos = pacientesNuevos;
            this.ocupacion = ocupacion;
        }

        public long getCitasHoy() { return citasHoy; }
        public long getCitasSemana() { return citasSemana; }
        public long getPacientesNuevos() { return pacientesNuevos; }
        public int getOcupacion() { return ocupacion; }
    }
}