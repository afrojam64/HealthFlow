package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "excepciones_agenda")
public class AgendaException {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @NotNull
  @Column(name = "profesional_id", nullable = false, columnDefinition = "uuid")
  private UUID professionalId;

  @NotNull
  @Column(name = "fecha_especifica", nullable = false)
  private LocalDate date;

  @Column(name = "hora_inicio")
  private LocalTime startTime; // null => todo el día

  @Column(name = "hora_fin")
  private LocalTime endTime;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false, length = 20)
  private ExceptionType type;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public UUID getProfessionalId() { return professionalId; }
  public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }
  public LocalTime getStartTime() { return startTime; }
  public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
  public ExceptionType getType() { return type; }
  public void setType(ExceptionType type) { this.type = type; }
}
