package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "disponibilidad_semanal")
public class WeeklyAvailability extends BaseEntity {

    @NotNull
    @Column(name = "profesional_id", nullable = false, columnDefinition = "uuid")
    private UUID professionalId;

    @NotNull
    @Column(name = "fecha_inicio_semana", nullable = false)
    private LocalDate weekStartDate; // Fecha de inicio de la semana (ej: 2026-03-10)

    @NotNull
    @Column(name = "dia_semana", nullable = false)
    private Integer dayOfWeek; // 1=Lunes, 2=Martes, ..., 7=Domingo

    @Column(name = "hora_inicio")
    private LocalTime startTime;

    @Column(name = "hora_fin")
    private LocalTime endTime;

    @Column(name = "activo")
    private Boolean active = true;

    // Getters y Setters
    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }

    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
