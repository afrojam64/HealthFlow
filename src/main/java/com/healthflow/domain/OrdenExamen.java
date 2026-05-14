package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orden_examen")
public class OrdenExamen extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Professional professional;

    @Column(name = "fecha_solicitud", nullable = false)
    private OffsetDateTime fechaSolicitud;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "observaciones_generales", columnDefinition = "TEXT")
    private String observacionesGenerales;

    @Column(name = "documento_id")
    private UUID documentoId; // referencia al PDF guardado en tabla documentos

    @OneToMany(mappedBy = "ordenExamen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenExamenDetalle> detalles = new ArrayList<>();

    // Getters y Setters
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

    public OffsetDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(OffsetDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacionesGenerales() { return observacionesGenerales; }
    public void setObservacionesGenerales(String observacionesGenerales) { this.observacionesGenerales = observacionesGenerales; }

    public UUID getDocumentoId() { return documentoId; }
    public void setDocumentoId(UUID documentoId) { this.documentoId = documentoId; }

    public List<OrdenExamenDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<OrdenExamenDetalle> detalles) { this.detalles = detalles; }
}