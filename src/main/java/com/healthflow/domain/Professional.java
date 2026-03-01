package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(name = "profesionales")
public class Professional {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "usuario_id", columnDefinition = "uuid")
  private UUID userId; // MVP: relación simple. Puedes migrar a @OneToOne si quieres.

  @NotBlank
  @Column(name = "nombre_completo", nullable = false, length = 150)
  private String fullName;

  @NotBlank
  @Column(name = "registro_medico", unique = true, nullable = false, length = 50)
  private String medicalRegistry;

  @NotBlank
  @Column(name = "especialidad", nullable = false, length = 100)
  private String specialty;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getMedicalRegistry() { return medicalRegistry; }
  public void setMedicalRegistry(String medicalRegistry) { this.medicalRegistry = medicalRegistry; }
  public String getSpecialty() { return specialty; }
  public void setSpecialty(String specialty) { this.specialty = specialty; }
}
