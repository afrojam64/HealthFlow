package com.healthflow.api.dto.rips;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

public class RipsDTO {

    @JsonProperty("fechaGeneracion")
    private LocalDate fechaGeneracion;

    @JsonProperty("nitProfesional")
    private String nitProfesional;

    @JsonProperty("nombreProfesional")
    private String nombreProfesional;

    @JsonProperty("registroRethus")
    private String registroRethus;

    @JsonProperty("consultas")
    private List<RipsConsultaDTO> consultas;

    public RipsDTO() {}

    // Getters y setters
    public LocalDate getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDate fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public String getNitProfesional() { return nitProfesional; }
    public void setNitProfesional(String nitProfesional) { this.nitProfesional = nitProfesional; }

    public String getNombreProfesional() { return nombreProfesional; }
    public void setNombreProfesional(String nombreProfesional) { this.nombreProfesional = nombreProfesional; }

    public String getRegistroRethus() { return registroRethus; }
    public void setRegistroRethus(String registroRethus) { this.registroRethus = registroRethus; }

    public List<RipsConsultaDTO> getConsultas() { return consultas; }
    public void setConsultas(List<RipsConsultaDTO> consultas) { this.consultas = consultas; }
}