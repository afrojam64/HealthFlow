package com.healthflow.web;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import com.healthflow.service.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorAgendaController {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final WeeklyAvailabilityRepository weeklyAvailabilityRepository;
    private final AvailabilityBaseRepository availabilityBaseRepository;
    private final AppointmentRepository appointmentRepository;
    private final ZoneId zoneId;
    private final int slotMinutes;

    public DoctorAgendaController(
            ProfessionalRepository professionalRepository,
            UserRepository userRepository,
            WeeklyAvailabilityRepository weeklyAvailabilityRepository,
            AvailabilityBaseRepository availabilityBaseRepository,
            AppointmentRepository appointmentRepository,
            @Value("${healthflow.timezone:America/Bogota}") String tz,
            @Value("${healthflow.appointment.slotMinutes:30}") int slotMinutes) {
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.weeklyAvailabilityRepository = weeklyAvailabilityRepository;
        this.availabilityBaseRepository = availabilityBaseRepository;
        this.appointmentRepository = appointmentRepository;
        this.zoneId = ZoneId.of(tz);
        this.slotMinutes = slotMinutes;
    }

    @GetMapping("/agenda")
    public String agenda(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));

        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        LocalDate today = LocalDate.now(zoneId);
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue() - 1;

        int prevMonth = currentMonth == 0 ? 11 : currentMonth - 1;
        int nextMonth = currentMonth == 11 ? 0 : currentMonth + 1;

        LocalDate startOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Variables necesarias para el layout (copiadas de PatientController)
        String fechaActual = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " de " +
                today.getYear();

        String fechaTablaCitas = today.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + ", " +
                today.getDayOfMonth() + " de " +
                today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        DashboardStats stats = new DashboardStats(0, 0, 0, 0);
        List<Object> proximasCitas = new ArrayList<>();

        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("fechaActual", fechaActual);
        model.addAttribute("fechaTablaCitas", fechaTablaCitas);
        model.addAttribute("proximasCitas", proximasCitas);
        model.addAttribute("prevDate", today.minusDays(1));
        model.addAttribute("nextDate", today.plusDays(1));
        model.addAttribute("today", today);

        // Datos específicos de la agenda
        model.addAttribute("professionalId", professional.getId());
        model.addAttribute("professionalName", professional.getFullName());
        model.addAttribute("title", "Agenda - HealthFlow");
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentMonthName", monthNames[currentMonth]);
        model.addAttribute("prevMonthName", monthNames[prevMonth]);
        model.addAttribute("nextMonthName", monthNames[nextMonth]);
        model.addAttribute("startOfCurrentWeek", startOfCurrentWeek);
        model.addAttribute("professionalSlug", professional.getSlug());
        model.addAttribute("slotMinutes", slotMinutes); // ← nuevo
        model.addAttribute("contenido", "doctor/agenda");

        return "fragments/layout";
    }

    @PostMapping("/agenda/save")
    @Transactional
    @ResponseBody
    public String saveAvailability(@RequestBody DisponibilidadRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        Professional professional = professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));

        LocalDate today = LocalDate.now(zoneId);
        LocalTime now = LocalTime.now(zoneId);
        LocalDate startOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (Boolean.TRUE.equals(request.getMismaFranja())) {
            // Modo: misma franja para todas las semanas seleccionadas
            List<String> semanas = request.getSemanas();
            if (semanas == null || semanas.isEmpty()) {
                throw new DomainException("Debes seleccionar al menos una semana.");
            }

            List<LocalDate> weekStarts = semanas.stream()
                    .map(LocalDate::parse)
                    .collect(Collectors.toList());

            for (LocalDate weekStart : weekStarts) {
                if (weekStart.isBefore(startOfCurrentWeek)) {
                    throw new DomainException("No se puede gestionar la agenda de semanas pasadas.");
                }
            }

            LocalTime startTime = LocalTime.parse(request.getHoraInicio());
            LocalTime endTime = LocalTime.parse(request.getHoraFin());

            boolean isEditingCurrentWeek = weekStarts.stream().anyMatch(ws -> ws.isEqual(startOfCurrentWeek));
            if (isEditingCurrentWeek && startTime.isBefore(now) && today.isEqual(LocalDate.now(zoneId))) {
                throw new DomainException("La hora de inicio ya ha pasado para hoy. Por favor, configure el horario de hoy manualmente usando la opción 'Configurar por día'.");
            }

            // Validar citas existentes en los días afectados
            for (LocalDate weekStart : weekStarts) {
                for (int day = 0; day < 7; day++) {
                    LocalDate currentDay = weekStart.plusDays(day);
                    if (currentDay.isBefore(today)) continue;
                    if (hasAppointmentsInRange(professional.getId(), currentDay, startTime, endTime)) {
                        throw new DomainException("No se puede modificar el horario del " + currentDay +
                                " porque ya hay citas agendadas en ese rango.");
                    }
                }
            }

            // Guardar
            for (LocalDate weekStart : weekStarts) {
                weeklyAvailabilityRepository.deleteByProfessionalIdAndWeekStartDate(professional.getId(), weekStart);
                for (int day = 0; day < 7; day++) {
                    LocalDate currentDay = weekStart.plusDays(day);
                    if (currentDay.isBefore(today)) continue;
                    WeeklyAvailability wa = new WeeklyAvailability();
                    wa.setProfessionalId(professional.getId());
                    wa.setWeekStartDate(weekStart);
                    wa.setDayOfWeek(currentDay.getDayOfWeek().getValue());
                    wa.setStartTime(startTime);
                    wa.setEndTime(endTime);
                    wa.setActive(true);
                    weeklyAvailabilityRepository.save(wa);
                }
            }
            return "Disponibilidad guardada correctamente.";

        } else {
            // Modo: configuración por día individual
            List<DiaConfig> dias = request.getDias();
            if (dias == null || dias.isEmpty()) {
                throw new DomainException("No se recibieron configuraciones por día.");
            }

            // Validar cada día
            for (DiaConfig dia : dias) {
                LocalDate fecha = LocalDate.parse(dia.getFecha());
                if (fecha.isBefore(today)) {
                    throw new DomainException("No se puede configurar un día pasado: " + fecha);
                }
                if (dia.getActivo() != null && !dia.getActivo()) continue;
                LocalTime startTime = LocalTime.parse(dia.getHoraInicio());
                LocalTime endTime = LocalTime.parse(dia.getHoraFin());
                if (fecha.isEqual(today) && startTime.isBefore(now)) {
                    throw new DomainException("No se puede configurar una hora pasada para el día de hoy.");
                }
                if (hasAppointmentsInRange(professional.getId(), fecha, startTime, endTime)) {
                    throw new DomainException("No se puede modificar el horario del " + fecha +
                            " porque ya hay citas agendadas en ese rango.");
                }
            }

            // Agrupar por semana y guardar
            Map<LocalDate, List<DiaConfig>> porSemana = dias.stream()
                    .collect(Collectors.groupingBy(d -> {
                        LocalDate fecha = LocalDate.parse(d.getFecha());
                        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    }));

            for (Map.Entry<LocalDate, List<DiaConfig>> entry : porSemana.entrySet()) {
                LocalDate weekStart = entry.getKey();
                weeklyAvailabilityRepository.deleteByProfessionalIdAndWeekStartDate(professional.getId(), weekStart);
                for (DiaConfig dia : entry.getValue()) {
                    LocalDate fecha = LocalDate.parse(dia.getFecha());
                    if (fecha.isBefore(today)) continue;
                    if (dia.getActivo() != null && !dia.getActivo()) continue;
                    LocalTime startTime = LocalTime.parse(dia.getHoraInicio());
                    LocalTime endTime = LocalTime.parse(dia.getHoraFin());

                    WeeklyAvailability wa = new WeeklyAvailability();
                    wa.setProfessionalId(professional.getId());
                    wa.setWeekStartDate(weekStart);
                    wa.setDayOfWeek(fecha.getDayOfWeek().getValue());
                    wa.setStartTime(startTime);
                    wa.setEndTime(endTime);
                    wa.setActive(true);
                    weeklyAvailabilityRepository.save(wa);
                }
            }
            return "Disponibilidad guardada correctamente.";
        }
    }

    // Método auxiliar para verificar si hay citas en un rango horario de un día
    private boolean hasAppointmentsInRange(UUID professionalId, LocalDate date, LocalTime start, LocalTime end) {
        OffsetDateTime startOfDay = date.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
        List<Appointment> appointments = appointmentRepository.findActiveByProfessionalIdAndDateTimeBetween(professionalId, startOfDay, endOfDay);
        for (Appointment a : appointments) {
            LocalTime apptTime = a.getDateTime().atZoneSameInstant(zoneId).toLocalTime();
            // Si la cita está dentro del rango (incluyendo extremos)
            if (!apptTime.isBefore(start) && !apptTime.isAfter(end)) {
                return true;
            }
        }
        return false;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    public static class DisponibilidadRequest {
        private List<String> semanas;
        private Boolean mismaFranja;
        private String horaInicio;
        private String horaFin;
        private List<DiaConfig> dias;

        public List<String> getSemanas() { return semanas; }
        public void setSemanas(List<String> semanas) { this.semanas = semanas; }
        public Boolean getMismaFranja() { return mismaFranja; }
        public void setMismaFranja(Boolean mismaFranja) { this.mismaFranja = mismaFranja; }
        public String getHoraInicio() { return horaInicio; }
        public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
        public String getHoraFin() { return horaFin; }
        public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
        public List<DiaConfig> getDias() { return dias; }
        public void setDias(List<DiaConfig> dias) { this.dias = dias; }
    }

    public static class DiaConfig {
        private String fecha;
        private String horaInicio;
        private String horaFin;
        private Boolean activo;

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        public String getHoraInicio() { return horaInicio; }
        public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
        public String getHoraFin() { return horaFin; }
        public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
        public Boolean getActivo() { return activo; }
        public void setActivo(Boolean activo) { this.activo = activo; }
    }

    // Clase interna para stats (igual que en PatientController)
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

    @GetMapping("/api/disponibilidad/mes")
    @ResponseBody
    public Map<String, Object> getMonthAvailability(@RequestParam("year") int year,
                                                    @RequestParam("month") int month,
                                                    @RequestParam("professionalId") UUID professionalId) {
        // 1. Obtener todos los días del mes
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        // 2. Obtener disponibilidad base (recurrente) para todos los días de la semana
        List<AvailabilityBase> baseList = availabilityBaseRepository
                .findByProfessionalIdOrderByDayOfWeekAscStartTimeAsc(professionalId);
        // Agrupar base por día de semana (1=lunes, ..., 7=domingo)
        Map<Integer, List<AvailabilityBase>> baseByDay = baseList.stream()
                .collect(Collectors.groupingBy(AvailabilityBase::getDayOfWeek));

        // 3. Obtener excepciones semanales para el rango del mes
        List<WeeklyAvailability> exceptions = weeklyAvailabilityRepository
                .findByProfessionalIdAndWeekStartDateBetween(professionalId, firstDay, lastDay);
        // Agrupar por fecha (día exacto) para fácil acceso
        Map<LocalDate, List<WeeklyAvailability>> exceptionsByDate = exceptions.stream()
                .collect(Collectors.groupingBy(wa -> {
                    return wa.getWeekStartDate().plusDays(wa.getDayOfWeek() - 1);
                }));

        // 4. Construir la respuesta para cada día del mes
        List<Map<String, Object>> daysData = new ArrayList<>();
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("date", date.toString());
            dayInfo.put("dayOfWeek", date.getDayOfWeek().getValue());

            List<WeeklyAvailability> ex = exceptionsByDate.get(date);
            if (ex != null && !ex.isEmpty()) {
                WeeklyAvailability wa = ex.get(0);
                dayInfo.put("isException", true);
                dayInfo.put("exceptionId", wa.getId());
                dayInfo.put("startTime", wa.getStartTime() != null ? wa.getStartTime().toString() : null);
                dayInfo.put("endTime", wa.getEndTime() != null ? wa.getEndTime().toString() : null);
                dayInfo.put("active", wa.getActive());
            } else {
                int dow = date.getDayOfWeek().getValue();
                List<AvailabilityBase> baseForDay = baseByDay.getOrDefault(dow, List.of());
                if (baseForDay.isEmpty()) {
                    dayInfo.put("isException", false);
                    dayInfo.put("hasBase", false);
                } else {
                    dayInfo.put("isException", false);
                    dayInfo.put("hasBase", true);
                    List<Map<String, String>> ranges = baseForDay.stream()
                            .map(b -> Map.of("start", b.getStartTime().toString(),
                                    "end", b.getEndTime().toString()))
                            .collect(Collectors.toList());
                    dayInfo.put("ranges", ranges);
                }
            }
            daysData.add(dayInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("days", daysData);
        return response;
    }
}