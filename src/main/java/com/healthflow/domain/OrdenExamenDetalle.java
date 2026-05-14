package com.healthflow.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "orden_examen_detalle")
public class OrdenExamenDetalle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private OrdenExamen ordenExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id")
    private CatalogoExamen examen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id")
    private PerfilClinico perfil;

    @Column(name = "cups_codigo", nullable = false, length = 20)
    private String cupsCodigo;

    @Column(name = "nombre_examen", nullable = false, length = 255)
    private String nombreExamen;

    @Column(name = "instrucciones_especificas", columnDefinition = "TEXT")
    private String instruccionesEspecificas;

    // Getters y Setters
    public OrdenExamen getOrdenExamen() { return ordenExamen; }
    public void setOrdenExamen(OrdenExamen ordenExamen) { this.ordenExamen = ordenExamen; }

    public CatalogoExamen getExamen() { return examen; }
    public void setExamen(CatalogoExamen examen) { this.examen = examen; }

    public PerfilClinico getPerfil() { return perfil; }
    public void setPerfil(PerfilClinico perfil) { this.perfil = perfil; }

    public String getCupsCodigo() { return cupsCodigo; }
    public void setCupsCodigo(String cupsCodigo) { this.cupsCodigo = cupsCodigo; }

    public String getNombreExamen() { return nombreExamen; }
    public void setNombreExamen(String nombreExamen) { this.nombreExamen = nombreExamen; }

    public String getInstruccionesEspecificas() { return instruccionesEspecificas; }
    public void setInstruccionesEspecificas(String instruccionesEspecificas) { this.instruccionesEspecificas = instruccionesEspecificas; }
}