package com.healthflow.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consultas_hc")
public class MedicalRecord extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "evolucion", columnDefinition = "TEXT")
    private String evolution;

    @Column(name = "prescripcion", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "dx_principal", length = 10)
    private String mainDiagnosis;

    @Column(name = "bloqueado", nullable = false)
    private Boolean locked = false;

    // Relaciones con catálogos RIPS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalidad_consulta_id")
    private CatalogoFinalidadConsulta finalidadConsulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causa_externa_id")
    private CatalogoCausaExterna causaExterna;

    // Campos RIPS adicionales
    @Column(name = "modalidad_consulta", length = 2)
    private String modalidadConsulta; // Tabla ModalidadAtencion (01=Presencial, 02=Telemedicina, etc.)

    @Column(name = "grupo_servicios", length = 2)
    private String grupoServicios; // Tabla GrupoServicios

    @Column(name = "via_ingreso", length = 2)
    private String viaIngreso; // Tabla ViaIngresoUsuario

    @Column(name = "tipo_diagnostico", length = 1)
    private String tipoDiagnostico; // 1=Impresión diagnóstica, 2=Confirmado nuevo, 3=Confirmado repetido

    @Column(name = "valor_servicio")
    private BigDecimal valorServicio;

    @Column(name = "cuota_moderadora")
    private BigDecimal cuotaModeradora;

    @Column(name = "copago")
    private BigDecimal copago;

    @Column(name = "codigo_cups", length = 10)
    private String codigoCups; // Código CUPS de la consulta

    @Column(name = "dx_relacionado1", length = 10)
    private String relatedDiagnosis1;

    @Column(name = "dx_relacionado2", length = 10)
    private String relatedDiagnosis2;

    @Column(name = "dx_complicacion", length = 10)
    private String complicationDiagnosis;

    @Column(name = "enfermedad_actual", columnDefinition = "TEXT")
    private String enfermedadActual;

    @Column(name = "examen_fisico", columnDefinition = "TEXT")
    private String examenFisico;

    @Column(name = "concepto", columnDefinition = "TEXT")
    private String concepto;

    // Getters y setters para los nuevos campos
    public String getEnfermedadActual() { return enfermedadActual; }
    public void setEnfermedadActual(String enfermedadActual) { this.enfermedadActual = enfermedadActual; }

    public String getExamenFisico() { return examenFisico; }
    public void setExamenFisico(String examenFisico) { this.examenFisico = examenFisico; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getEvolution() { return evolution; }
    public void setEvolution(String evolution) { this.evolution = evolution; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getMainDiagnosis() { return mainDiagnosis; }
    public void setMainDiagnosis(String mainDiagnosis) { this.mainDiagnosis = mainDiagnosis; }

    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }

    public CatalogoFinalidadConsulta getFinalidadConsulta() { return finalidadConsulta; }
    public void setFinalidadConsulta(CatalogoFinalidadConsulta finalidadConsulta) { this.finalidadConsulta = finalidadConsulta; }

    public CatalogoCausaExterna getCausaExterna() { return causaExterna; }
    public void setCausaExterna(CatalogoCausaExterna causaExterna) { this.causaExterna = causaExterna; }

    public String getModalidadConsulta() { return modalidadConsulta; }
    public void setModalidadConsulta(String modalidadConsulta) { this.modalidadConsulta = modalidadConsulta; }

    public String getGrupoServicios() { return grupoServicios; }
    public void setGrupoServicios(String grupoServicios) { this.grupoServicios = grupoServicios; }

    public String getViaIngreso() { return viaIngreso; }
    public void setViaIngreso(String viaIngreso) { this.viaIngreso = viaIngreso; }

    public String getTipoDiagnostico() { return tipoDiagnostico; }
    public void setTipoDiagnostico(String tipoDiagnostico) { this.tipoDiagnostico = tipoDiagnostico; }

    public BigDecimal getValorServicio() { return valorServicio; }
    public void setValorServicio(BigDecimal valorServicio) { this.valorServicio = valorServicio; }

    public BigDecimal getCuotaModeradora() { return cuotaModeradora; }
    public void setCuotaModeradora(BigDecimal cuotaModeradora) { this.cuotaModeradora = cuotaModeradora; }

    public BigDecimal getCopago() { return copago; }
    public void setCopago(BigDecimal copago) { this.copago = copago; }

    public String getCodigoCups() { return codigoCups; }
    public void setCodigoCups(String codigoCups) { this.codigoCups = codigoCups; }

    public String getRelatedDiagnosis1() { return relatedDiagnosis1; }
    public void setRelatedDiagnosis1(String relatedDiagnosis1) { this.relatedDiagnosis1 = relatedDiagnosis1; }

    public String getRelatedDiagnosis2() { return relatedDiagnosis2; }
    public void setRelatedDiagnosis2(String relatedDiagnosis2) { this.relatedDiagnosis2 = relatedDiagnosis2; }

    public String getComplicationDiagnosis() { return complicationDiagnosis; }
    public void setComplicationDiagnosis(String complicationDiagnosis) { this.complicationDiagnosis = complicationDiagnosis; }
}