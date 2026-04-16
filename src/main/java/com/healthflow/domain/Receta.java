package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recetas")
public class Receta extends BaseEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 50)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Professional professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Appointment appointment;

    @Column(name = "fecha_emision", nullable = false)
    private OffsetDateTime fechaEmision;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVA";

    @Column(name = "token", unique = true)
    private UUID token;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Getters y setters
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public OffsetDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(OffsetDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public OffsetDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(OffsetDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public UUID getToken() { return token; }
    public void setToken(UUID token) { this.token = token; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}