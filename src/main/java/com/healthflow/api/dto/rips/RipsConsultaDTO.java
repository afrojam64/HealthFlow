package com.healthflow.api.dto.rips;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;

public class RipsConsultaDTO {

    @JsonProperty("fechaInicioAtencion")
    private LocalDate fechaInicioAtencion;

    @JsonProperty("horaInicioAtencion")
    private LocalTime horaInicioAtencion;

    @JsonProperty("codigoConsulta")
    private String codigoConsulta; // Código CUPS de la consulta

    @JsonProperty("finalidadConsulta")
    private String finalidadConsulta; // Código de finalidad (catálogo)

    @JsonProperty("causaExterna")
    private String causaExterna; // Código de causa externa (catálogo)

    @JsonProperty("codDiagnosticoPrincipal")
    private String codDiagnosticoPrincipal; // CIE-10 principal

    @JsonProperty("codDiagnosticoRelacionado1")
    private String codDiagnosticoRelacionado1;

    @JsonProperty("codDiagnosticoRelacionado2")
    private String codDiagnosticoRelacionado2;

    @JsonProperty("codDiagnosticoComplicacion")
    private String codDiagnosticoComplicacion;

    @JsonProperty("tipoDocumentoIdentificacion")
    private String tipoDocumentoIdentificacion; // CC, TI, etc.

    @JsonProperty("numDocumentoIdentificacion")
    private String numDocumentoIdentificacion;

    @JsonProperty("vrServicio")
    private Double vrServicio;

    @JsonProperty("vrCuotaModeradora")
    private Double vrCuotaModeradora;

    @JsonProperty("vrCopago")
    private Double vrCopago;

    // Constructores, getters y setters
    public RipsConsultaDTO() {}

    // Getters y setters
    public LocalDate getFechaInicioAtencion() { return fechaInicioAtencion; }
    public void setFechaInicioAtencion(LocalDate fechaInicioAtencion) { this.fechaInicioAtencion = fechaInicioAtencion; }

    public LocalTime getHoraInicioAtencion() { return horaInicioAtencion; }
    public void setHoraInicioAtencion(LocalTime horaInicioAtencion) { this.horaInicioAtencion = horaInicioAtencion; }

    public String getCodigoConsulta() { return codigoConsulta; }
    public void setCodigoConsulta(String codigoConsulta) { this.codigoConsulta = codigoConsulta; }

    public String getFinalidadConsulta() { return finalidadConsulta; }
    public void setFinalidadConsulta(String finalidadConsulta) { this.finalidadConsulta = finalidadConsulta; }

    public String getCausaExterna() { return causaExterna; }
    public void setCausaExterna(String causaExterna) { this.causaExterna = causaExterna; }

    public String getCodDiagnosticoPrincipal() { return codDiagnosticoPrincipal; }
    public void setCodDiagnosticoPrincipal(String codDiagnosticoPrincipal) { this.codDiagnosticoPrincipal = codDiagnosticoPrincipal; }

    public String getCodDiagnosticoRelacionado1() { return codDiagnosticoRelacionado1; }
    public void setCodDiagnosticoRelacionado1(String codDiagnosticoRelacionado1) { this.codDiagnosticoRelacionado1 = codDiagnosticoRelacionado1; }

    public String getCodDiagnosticoRelacionado2() { return codDiagnosticoRelacionado2; }
    public void setCodDiagnosticoRelacionado2(String codDiagnosticoRelacionado2) { this.codDiagnosticoRelacionado2 = codDiagnosticoRelacionado2; }

    public String getCodDiagnosticoComplicacion() { return codDiagnosticoComplicacion; }
    public void setCodDiagnosticoComplicacion(String codDiagnosticoComplicacion) { this.codDiagnosticoComplicacion = codDiagnosticoComplicacion; }

    public String getTipoDocumentoIdentificacion() { return tipoDocumentoIdentificacion; }
    public void setTipoDocumentoIdentificacion(String tipoDocumentoIdentificacion) { this.tipoDocumentoIdentificacion = tipoDocumentoIdentificacion; }

    public String getNumDocumentoIdentificacion() { return numDocumentoIdentificacion; }
    public void setNumDocumentoIdentificacion(String numDocumentoIdentificacion) { this.numDocumentoIdentificacion = numDocumentoIdentificacion; }

    public Double getVrServicio() { return vrServicio; }
    public void setVrServicio(Double vrServicio) { this.vrServicio = vrServicio; }

    public Double getVrCuotaModeradora() { return vrCuotaModeradora; }
    public void setVrCuotaModeradora(Double vrCuotaModeradora) { this.vrCuotaModeradora = vrCuotaModeradora; }

    public Double getVrCopago() { return vrCopago; }
    public void setVrCopago(Double vrCopago) { this.vrCopago = vrCopago; }
}