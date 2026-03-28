package com.healthflow.web;

import com.healthflow.domain.AgendaException;
import com.healthflow.domain.ExceptionType;
import com.healthflow.domain.Professional;
import com.healthflow.repo.AgendaExceptionRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctor/agenda/exceptions")
public class AgendaExceptionController {

    private final AgendaExceptionRepository exceptionRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final ZoneId zoneId;

    public AgendaExceptionController(AgendaExceptionRepository exceptionRepository,
                                     ProfessionalRepository professionalRepository,
                                     UserRepository userRepository,
                                     @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.exceptionRepository = exceptionRepository;
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

    @PostMapping("/range")
    public List<AgendaException> createExceptions(@RequestBody CreateRangeRequest request) {
        UUID professionalId = getCurrentProfessionalId();
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new DomainException("Profesional no encontrado"));

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        ExceptionType type = request.getTipo();
        String reason = request.getMotivo();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        List<AgendaException> exceptions = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            // Validar que no se modifiquen días pasados
            if (date.isBefore(LocalDate.now(zoneId))) {
                throw new DomainException("No se puede agregar excepción para un día pasado: " + date);
            }
            AgendaException exception = new AgendaException();
            exception.setProfessional(professional);
            exception.setDate(date);
            exception.setType(type);
            if (type == ExceptionType.EXTRA) {
                exception.setStartTime(startTime);
                exception.setEndTime(endTime);
            }
            exception.setReason(reason);
            exceptions.add(exceptionRepository.save(exception));
        }
        return exceptions;
    }

    public static class CreateRangeRequest {
        private LocalDate startDate;
        private LocalDate endDate;
        private ExceptionType tipo;
        private LocalTime startTime;
        private LocalTime endTime;
        private String motivo;

        // Getters y setters
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public ExceptionType getTipo() { return tipo; }
        public void setTipo(ExceptionType tipo) { this.tipo = tipo; }
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }

    @DeleteMapping("/{id}")
    public void deleteException(@PathVariable UUID id) {
        UUID professionalId = getCurrentProfessionalId();
        AgendaException exception = exceptionRepository.findById(id)
                .orElseThrow(() -> new DomainException("Excepción no encontrada"));
        if (!exception.getProfessional().getId().equals(professionalId)) {
            throw new DomainException("No tienes permiso para eliminar esta excepción");
        }
        exceptionRepository.delete(exception);
    }

    public static class CreateExceptionRequest {
        private LocalDate fecha;
        private ExceptionType tipo;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private String motivo;

        // getters y setters
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public ExceptionType getTipo() { return tipo; }
        public void setTipo(ExceptionType tipo) { this.tipo = tipo; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }
}