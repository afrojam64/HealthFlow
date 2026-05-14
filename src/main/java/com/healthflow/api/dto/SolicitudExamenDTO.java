package com.healthflow.api.dto;

import java.util.UUID;

public class SolicitudExamenDTO {
    private UUID examenId;        // puede ser nulo si se usa perfilId
    private UUID perfilId;        // opcional, si se solicita un perfil completo
    private String instrucciones; // instrucciones específicas para este ítem

    // Getters y Setters
    public UUID getExamenId() { return examenId; }
    public void setExamenId(UUID examenId) { this.examenId = examenId; }

    public UUID getPerfilId() { return perfilId; }
    public void setPerfilId(UUID perfilId) { this.perfilId = perfilId; }

    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
}