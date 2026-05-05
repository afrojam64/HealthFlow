package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "asistente_permisos")
public class AsistentePermiso extends BaseEntity {

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "asistente_id", nullable = false)
    private UUID asistenteId;

    @Column(name = "permiso", nullable = false, length = 50)
    private String permiso;

    @Column(name = "concedido")
    private Boolean concedido = true;

    // Getters y Setters
    public UUID getMedicoId() { return medicoId; }
    public void setMedicoId(UUID medicoId) { this.medicoId = medicoId; }

    public UUID getAsistenteId() { return asistenteId; }
    public void setAsistenteId(UUID asistenteId) { this.asistenteId = asistenteId; }

    public String getPermiso() { return permiso; }
    public void setPermiso(String permiso) { this.permiso = permiso; }

    public Boolean getConcedido() { return concedido; }
    public void setConcedido(Boolean concedido) { this.concedido = concedido; }
}