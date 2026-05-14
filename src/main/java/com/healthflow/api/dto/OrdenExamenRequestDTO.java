package com.healthflow.api.dto;

import java.util.List;

public class OrdenExamenRequestDTO {
    private List<SolicitudExamenDTO> solicitudes;
    private String observacionesGenerales; // instrucciones globales

    // Getters y Setters
    public List<SolicitudExamenDTO> getSolicitudes() { return solicitudes; }
    public void setSolicitudes(List<SolicitudExamenDTO> solicitudes) { this.solicitudes = solicitudes; }

    public String getObservacionesGenerales() { return observacionesGenerales; }
    public void setObservacionesGenerales(String observacionesGenerales) { this.observacionesGenerales = observacionesGenerales; }
}