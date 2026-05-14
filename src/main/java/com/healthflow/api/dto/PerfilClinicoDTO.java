package com.healthflow.api.dto;

import java.util.List;
import java.util.UUID;

public class PerfilClinicoDTO {
    private UUID id;
    private String nombre;
    private String descripcion;
    private List<CatalogoExamenDTO> examenes;

    // Constructor vacío
    public PerfilClinicoDTO() {}

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<CatalogoExamenDTO> getExamenes() { return examenes; }
    public void setExamenes(List<CatalogoExamenDTO> examenes) { this.examenes = examenes; }
}