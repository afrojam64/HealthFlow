package com.healthflow.web;

import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.domain.WeeklyAvailability;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.repo.WeeklyAvailabilityRepository;
import com.healthflow.service.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorAgendaController {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final WeeklyAvailabilityRepository weeklyAvailabilityRepository;
    private final ZoneId zoneId;

    public DoctorAgendaController(
            ProfessionalRepository professionalRepository,
            UserRepository userRepository,
            WeeklyAvailabilityRepository weeklyAvailabilityRepository,
            @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.weeklyAvailabilityRepository = weeklyAvailabilityRepository;
        this.zoneId = ZoneId.of(tz);
    }

    @GetMapping("/agenda")
    public String agenda(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));

        Professional professional = professionalRepository.findByUser(user)
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));

        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        LocalDate today = LocalDate.now(zoneId);
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue() - 1;

        int prevMonth = currentMonth == 0 ? 11 : currentMonth - 1;
        int nextMonth = currentMonth == 11 ? 0 : currentMonth + 1;

        model.addAttribute("title", "Agenda - HealthFlow");
        model.addAttribute("professionalName", professional.getFullName());
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentMonthName", monthNames[currentMonth]);
        model.addAttribute("prevMonthName", monthNames[prevMonth]);
        model.addAttribute("nextMonthName", monthNames[nextMonth]);

        return "doctor/agenda";
    }

    @PostMapping("/agenda/save")
    @Transactional
    @ResponseBody
    public String saveAvailability(@RequestBody DisponibilidadRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));

        Professional professional = professionalRepository.findByUser(user)
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));

        List<String> semanas = request.getSemanas();
        if (semanas == null || semanas.isEmpty()) {
            throw new DomainException("Debes seleccionar al menos una semana.");
        }

        List<LocalDate> weekStarts = semanas.stream()
                .map(LocalDate::parse)
                .collect(Collectors.toList());

        // REGLA: No se puede modificar la disponibilidad de semanas pasadas
        LocalDate startOfCurrentWeek = LocalDate.now(zoneId).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (LocalDate weekStart : weekStarts) {
            if (weekStart.isBefore(startOfCurrentWeek)) {
                throw new DomainException("No se puede gestionar la agenda de semanas pasadas.");
            }
        }

        if (request.getMismaFranja()) {
            if (request.getHoraInicio() == null || request.getHoraInicio().isEmpty() ||
                    request.getHoraFin() == null || request.getHoraFin().isEmpty()) {
                throw new DomainException("Debes proporcionar hora de inicio y fin no vacías.");
            }
            LocalTime startTime = LocalTime.parse(request.getHoraInicio());
            LocalTime endTime = LocalTime.parse(request.getHoraFin());

            for (LocalDate weekStart : weekStarts) {
                weeklyAvailabilityRepository.deleteByProfessional_IdAndWeekStartDate(professional.getId(), weekStart);
                for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
                    WeeklyAvailability wa = new WeeklyAvailability();
                    wa.setProfessional(professional);
                    wa.setWeekStartDate(weekStart);
                    wa.setDayOfWeek(dayOfWeek);
                    wa.setStartTime(startTime);
                    wa.setEndTime(endTime);
                    wa.setActive(true);
                    weeklyAvailabilityRepository.save(wa);
                }
            }
            return "Disponibilidad guardada correctamente para " + semanas.size() + " semanas con horario único.";
        } else {
            List<DiaConfig> dias = request.getDias();
            if (dias == null || dias.isEmpty()) {
                throw new DomainException("No se recibieron configuraciones por día.");
            }

            for (DiaConfig dia : dias) {
                if (dia.getFecha() == null || dia.getFecha().isEmpty() ||
                        dia.getHoraInicio() == null || dia.getHoraInicio().isEmpty() ||
                        dia.getHoraFin() == null || dia.getHoraFin().isEmpty()) {
                    throw new DomainException("Todos los días deben tener fecha, hora inicio y hora fin completos.");
                }
            }

            Map<LocalDate, List<DiaConfig>> porSemana = dias.stream()
                    .collect(Collectors.groupingBy(d -> {
                        LocalDate fecha = LocalDate.parse(d.getFecha());
                        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    }));

            for (Map.Entry<LocalDate, List<DiaConfig>> entry : porSemana.entrySet()) {
                LocalDate weekStart = entry.getKey();
                weeklyAvailabilityRepository.deleteByProfessional_IdAndWeekStartDate(professional.getId(), weekStart);
                for (DiaConfig dia : entry.getValue()) {
                    if (dia.getActivo() != null && !dia.getActivo()) {
                        continue;
                    }
                    LocalDate fecha = LocalDate.parse(dia.getFecha());
                    int dayOfWeek = fecha.getDayOfWeek().getValue();
                    LocalTime startTime = LocalTime.parse(dia.getHoraInicio());
                    LocalTime endTime = LocalTime.parse(dia.getHoraFin());
                    WeeklyAvailability wa = new WeeklyAvailability();
                    wa.setProfessional(professional);

                    wa.setWeekStartDate(weekStart);
                    wa.setDayOfWeek(dayOfWeek);
                    wa.setStartTime(startTime);
                    wa.setEndTime(endTime);
                    wa.setActive(true);
                    weeklyAvailabilityRepository.save(wa);
                }
            }
            return "Disponibilidad guardada correctamente para " + dias.size() + " días.";
        }
    }

    // Clases internas para recibir JSON
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
}
