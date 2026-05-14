package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "perfiles_clinicos")
public class PerfilClinico extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo")
    private Boolean activo = true;

    @ManyToMany
    @JoinTable(
            name = "perfil_examen",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "examen_id")
    )
    private Set<CatalogoExamen> examenes = new HashSet<>();

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Set<CatalogoExamen> getExamenes() { return examenes; }
    public void setExamenes(Set<CatalogoExamen> examenes) { this.examenes = examenes; }
}