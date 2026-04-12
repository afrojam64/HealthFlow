package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "remisiones")
public class Remision extends BaseEntity {

    @Column(name = "cita_id", nullable = false)
    private UUID citaId;

    @Column(name = "token", nullable = false, unique = true)
    private UUID token;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(nullable = false, length = 20)
    private String prioridad;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String snapshot;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    // Getters y setters
    public UUID getCitaId() { return citaId; }
    public void setCitaId(UUID citaId) { this.citaId = citaId; }
    public UUID getToken() { return token; }
    public void setToken(UUID token) { this.token = token; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getSnapshot() { return snapshot; }
    public void setSnapshot(String snapshot) { this.snapshot = snapshot; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}