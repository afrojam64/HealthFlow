package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "catalogo_examenes")
public class CatalogoExamen extends BaseEntity {

    @Column(name = "codigo_cups", nullable = false, length = 20)
    private String codigoCups;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "modalidad", length = 50)
    private String modalidad;

    @Column(name = "region_anatomica", length = 100)
    private String regionAnatomica;

    @Column(name = "requiere_contraste")
    private Boolean requiereContraste = false;

    @Column(name = "tipo_muestra", length = 50)
    private String tipoMuestra;

    @Column(name = "version_cups", length = 10)
    private String versionCups;

    @Column(name = "fecha_vigencia")
    private LocalDate fechaVigencia;

    @Column(name = "activo")
    private Boolean activo = true;

    // Getters y Setters
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

    public String getVersionCups() { return versionCups; }
    public void setVersionCups(String versionCups) { this.versionCups = versionCups; }

    public LocalDate getFechaVigencia() { return fechaVigencia; }
    public void setFechaVigencia(LocalDate fechaVigencia) { this.fechaVigencia = fechaVigencia; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}