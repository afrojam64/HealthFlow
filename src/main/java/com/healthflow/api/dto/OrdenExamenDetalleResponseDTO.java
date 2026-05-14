package com.healthflow.api.dto;

import java.util.UUID;

public class OrdenExamenDetalleResponseDTO {
    private UUID id;
    private String cupsCodigo;
    private String nombreExamen;
    private String instruccionesEspecificas;

    // Constructor, getters y setters
    public OrdenExamenDetalleResponseDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCupsCodigo() { return cupsCodigo; }
    public void setCupsCodigo(String cupsCodigo) { this.cupsCodigo = cupsCodigo; }

    public String getNombreExamen() { return nombreExamen; }
    public void setNombreExamen(String nombreExamen) { this.nombreExamen = nombreExamen; }

    public String getInstruccionesEspecificas() { return instruccionesEspecificas; }
    public void setInstruccionesEspecificas(String instruccionesEspecificas) { this.instruccionesEspecificas = instruccionesEspecificas; }
}