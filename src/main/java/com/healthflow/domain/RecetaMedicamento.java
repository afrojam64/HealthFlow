package com.healthflow.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "receta_medicamentos")
public class RecetaMedicamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dosis", nullable = false, length = 100)
    private String dosis;

    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "duracion", length = 100)
    private String duracion;

    @Column(name = "instrucciones", columnDefinition = "TEXT")
    private String instrucciones;

    // Getters y setters
    public Receta getReceta() { return receta; }
    public void setReceta(Receta receta) { this.receta = receta; }

    public Medicamento getMedicamento() { return medicamento; }
    public void setMedicamento(Medicamento medicamento) { this.medicamento = medicamento; }

    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
}