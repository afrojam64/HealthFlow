package com.healthflow.api.dto;

import java.util.UUID;

public class CatalogoExamenDTO {
    private UUID id;
    private String codigoCups;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String modalidad;
    private String regionAnatomica;
    private Boolean requiereContraste;
    private String tipoMuestra;
    private Boolean activo;

    // Constructor vacío
    public CatalogoExamenDTO() {}

    // Constructor con campos principales
    public CatalogoExamenDTO(UUID id, String codigoCups, String nombre, String categoria) {
        this.id = id;
        this.codigoCups = codigoCups;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigoCups() { return codigoCups; }
    public void setCodigoCups(String codigoCups) { this.codigoCups = codigoCups; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getRegionAnatomica() { return regionAnatomica; }
    public void setRegionAnatomica(String regionAnatomica) { this.regionAnatomica = regionAnatomica; }

    public Boolean getRequiereContraste() { return requiereContraste; }
    public void setRequiereContraste(Boolean requiereContraste) { this.requiereContraste = requiereContraste; }

    public String getTipoMuestra() { return tipoMuestra; }
    public void setTipoMuestra(String tipoMuestra) { this.tipoMuestra = tipoMuestra; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}