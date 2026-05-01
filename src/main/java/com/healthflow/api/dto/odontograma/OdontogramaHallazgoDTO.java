package com.healthflow.api.dto.odontograma;

import java.util.UUID;

public class OdontogramaHallazgoDTO {
    private UUID id;               // opcional, para actualizaciones
    private UUID citaId;
    private Short diente;
    private String cara;
    private String tipoHallazgo;
    private Long cupsId;
    private String valorJson;      // JSON con datos adicionales
    private Boolean esInicial;

    // Getters y setters
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
}