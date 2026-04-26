package com.healthflow.api.dto.rips;

import java.util.List;
import java.util.Map;

public class RipsValidationResult {
    private int totalCitas;
    private int citasValidas;
    private int citasInvalidas;
    private List<Map<String, Object>> detalles; // puede contener id, paciente, razón

    // constructores, getters y setters
    public RipsValidationResult() {}

    public int getTotalCitas() { return totalCitas; }
    public void setTotalCitas(int totalCitas) { this.totalCitas = totalCitas; }
    public int getCitasValidas() { return citasValidas; }
    public void setCitasValidas(int citasValidas) { this.citasValidas = citasValidas; }
    public int getCitasInvalidas() { return citasInvalidas; }
    public void setCitasInvalidas(int citasInvalidas) { this.citasInvalidas = citasInvalidas; }
    public List<Map<String, Object>> getDetalles() { return detalles; }
    public void setDetalles(List<Map<String, Object>> detalles) { this.detalles = detalles; }
}