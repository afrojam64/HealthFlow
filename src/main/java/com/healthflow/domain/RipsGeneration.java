package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rips_generations")
public class RipsGeneration extends BaseEntity {

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "tipo_generacion", nullable = false, length = 10)
    private String tipoGeneracion;

    @Column(name = "num_factura", length = 50)
    private String numFactura;

    @Column(name = "tipo_nota", length = 10)
    private String tipoNota;

    @Column(name = "num_nota", length = 50)
    private String numNota;

    @Column(name = "archivo_path", length = 500)
    private String archivoPath;

    @Column(name = "automatica")
    private boolean automatica = false;

    @Column(name = "cuv", length = 50)
    private String cuv;

    @Column(name = "total_registros")
    private int totalRegistros;

    // Getters y Setters (generarlos o usar Lombok)
    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }
    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public String getTipoGeneracion() { return tipoGeneracion; }
    public void setTipoGeneracion(String tipoGeneracion) { this.tipoGeneracion = tipoGeneracion; }
    public String getNumFactura() { return numFactura; }
    public void setNumFactura(String numFactura) { this.numFactura = numFactura; }
    public String getTipoNota() { return tipoNota; }
    public void setTipoNota(String tipoNota) { this.tipoNota = tipoNota; }
    public String getNumNota() { return numNota; }
    public void setNumNota(String numNota) { this.numNota = numNota; }
    public String getArchivoPath() { return archivoPath; }
    public void setArchivoPath(String archivoPath) { this.archivoPath = archivoPath; }
    public boolean isAutomatica() { return automatica; }
    public void setAutomatica(boolean automatica) { this.automatica = automatica; }
    public String getCuv() { return cuv; }
    public void setCuv(String cuv) { this.cuv = cuv; }
    public int getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(int totalRegistros) { this.totalRegistros = totalRegistros; }
}