package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "disponibilidad_base")
public class AvailabilityBase {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @NotNull
  @Column(name = "profesional_id", nullable = false, columnDefinition = "uuid")
  private UUID professionalId;

  @Min(0) @Max(6)
  @Column(name = "dia_semana", nullable = false)
  private int dayOfWeek; // 0=Dom ... 6=Sab (según doc)

  @NotNull
  @Column(name = "hora_inicio", nullable = false)
  private LocalTime startTime;

  @NotNull
  @Column(name = "hora_fin", nullable = false)
  private LocalTime endTime;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public UUID getProfessionalId() { return professionalId; }
  public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
  public int getDayOfWeek() { return dayOfWeek; }
  public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
  public LocalTime getStartTime() { return startTime; }
  public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
