package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consultas_hc")
public class MedicalRecord {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Appointment appointment;

    @NotBlank
    @Column(name = "dx_principal", nullable = false, length = 4)
    private String primaryDiagnosis; // Código CIE-10

    @Column(name = "dx_relacionado1", length = 4)
    private String relatedDiagnosis1;

    @Column(name = "dx_relacionado2", length = 4)
    private String relatedDiagnosis2;

    @NotBlank
    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @NotBlank
    @Column(name = "evolucion", nullable = false, columnDefinition = "TEXT")
    private String evolution;

    @Column(name = "prescripcion", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // Getters y Setters
    public UUID getId() { return id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getPrimaryDiagnosis() { return primaryDiagnosis; }
    public void setPrimaryDiagnosis(String primaryDiagnosis) { this.primaryDiagnosis = primaryDiagnosis; }

    public String getRelatedDiagnosis1() { return relatedDiagnosis1; }
    public void setRelatedDiagnosis1(String relatedDiagnosis1) { this.relatedDiagnosis1 = relatedDiagnosis1; }

    public String getRelatedDiagnosis2() { return relatedDiagnosis2; }
    public void setRelatedDiagnosis2(String relatedDiagnosis2) { this.relatedDiagnosis2 = relatedDiagnosis2; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getEvolution() { return evolution; }
    public void setEvolution(String evolution) { this.evolution = evolution; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}