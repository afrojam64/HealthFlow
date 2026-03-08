package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

@Entity
@Table(name = "disponibilidad_base")
public class AvailabilityBase extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Professional professional;

    @Min(0) @Max(6)
    @Column(name = "dia_semana", nullable = false)
    private int dayOfWeek; // 0=Dom ... 6=Sab (según doc)

    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime endTime;

    // Getters y Setters
    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
