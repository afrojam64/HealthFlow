package com.healthflow.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "odontograma_hallazgos")
public class OdontogramaHallazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cita_id", nullable = false)
    private UUID citaId;

    @Column(name = "diente", nullable = false)
    private Short diente;

    @Column(name = "cara", length = 20)
    private String cara;

    @Column(name = "tipo_hallazgo", nullable = false, length = 50)
    private String tipoHallazgo;

    @Column(name = "cups_id")
    private Long cupsId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_json", columnDefinition = "jsonb")
    private String valorJson;  // Almacena datos específicos (periodontograma, etc.)

    @Column(name = "es_inicial", nullable = false)
    private Boolean esInicial = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCitaId() { return citaId; }
    public void setCitaId(UUID citaId) { this.citaId = citaId; }

    public Short getDiente() { return diente; }
    public void setDiente(Short diente) { this.diente = diente; }

    public String getCara() { return cara; }
    public void setCara(String cara) { this.cara = cara; }

    public String getTipoHallazgo() { return tipoHallazgo; }
    public void setTipoHallazgo(String tipoHallazgo) { this.tipoHallazgo = tipoHallazgo; }

    public Long getCupsId() { return cupsId; }
    public void setCupsId(Long cupsId) { this.cupsId = cupsId; }

    public String getValorJson() { return valorJson; }
    public void setValorJson(String valorJson) { this.valorJson = valorJson; }

    public Boolean getEsInicial() { return esInicial; }
    public void setEsInicial(Boolean esInicial) { this.esInicial = esInicial; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}