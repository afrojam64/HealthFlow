package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "catalogo_causa_externa")
public class CatalogoCausaExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(unique = true, nullable = false, length = 2)
    private String codigo;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Constructores
    public CatalogoCausaExterna() {}

    public CatalogoCausaExterna(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}