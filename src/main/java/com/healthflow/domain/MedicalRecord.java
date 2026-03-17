package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull; // Mantener si se usa en otros campos

@Entity
@Table(name = "consultas_hc")
public class MedicalRecord extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Appointment appointment;

    // @NotNull ELIMINADO: Permitir que sea nulo para borradores
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String reason;

    // @NotNull ELIMINADO: Permitir que sea nulo para borradores
    @Column(name = "evolucion", columnDefinition = "TEXT")
    private String evolution;

    @Column(name = "prescripcion", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "dx_principal", length = 10)
    private String mainDiagnosis;

    @NotNull // Este campo sí debe ser NotNull
    @Column(name = "bloqueado", nullable = false)
    private Boolean locked = false;

    // Getters y Setters

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEvolution() {
        return evolution;
    }

    public void setEvolution(String evolution) {
        this.evolution = evolution;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getMainDiagnosis() {
        return mainDiagnosis;
    }

    public void setMainDiagnosis(String mainDiagnosis) {
        this.mainDiagnosis = mainDiagnosis;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
}
