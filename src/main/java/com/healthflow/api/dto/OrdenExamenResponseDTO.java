package com.healthflow.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class OrdenExamenResponseDTO {
    private UUID id;
    private UUID citaId;
    private UUID pacienteId;
    private UUID profesionalId;
    private OffsetDateTime fechaSolicitud;
    private String estado;
    private String observacionesGenerales;
    private UUID documentoId; // ID del PDF generado
    private List<OrdenExamenDetalleResponseDTO> detalles;

    // Constructor, getters y setters
    public OrdenExamenResponseDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCitaId() { return citaId; }
    public void setCitaId(UUID citaId) { this.citaId = citaId; }

    public UUID getPacienteId() { return pacienteId; }
    public void setPacienteId(UUID pacienteId) { this.pacienteId = pacienteId; }

    public UUID getProfesionalId() { return profesionalId; }
    public void setProfesionalId(UUID profesionalId) { this.profesionalId = profesionalId; }

    public OffsetDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(OffsetDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacionesGenerales() { return observacionesGenerales; }
    public void setObservacionesGenerales(String observacionesGenerales) { this.observacionesGenerales = observacionesGenerales; }

    public UUID getDocumentoId() { return documentoId; }
    public void setDocumentoId(UUID documentoId) { this.documentoId = documentoId; }

    public List<OrdenExamenDetalleResponseDTO> getDetalles() { return detalles; }
    public void setDetalles(List<OrdenExamenDetalleResponseDTO> detalles) { this.detalles = detalles; }
}