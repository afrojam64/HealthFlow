package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paciente_token")
public class PacienteToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Patient patient;

    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "activo", nullable = false)
    private boolean active = true;

    // Constructor por defecto
    public PacienteToken() {}

    public PacienteToken(Patient patient, String token, LocalDateTime expiresAt) {
        this.patient = patient;
        this.token = token;
        this.expiresAt = expiresAt;
        this.active = true;
    }

    // Getters y Setters
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}