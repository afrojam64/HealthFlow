package com.healthflow.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "periodontogramas")
public class Periodontograma extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Professional professional;

    @Column(name = "fecha_examen", nullable = false)
    private LocalDate examDate;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mediciones_json", nullable = false, columnDefinition = "JSONB")
    private String measurementsJson;  // Se guardará como String JSON; se puede usar un Map en su lugar

    @Column(name = "diagnostico_base", length = 50)
    private String diagnosisBase;

    @Column(name = "subcategoria", length = 100)
    private String subcategory;

    @Column(name = "stage", length = 3)
    private String stage;

    @Column(name = "grade", length = 1)
    private String grade;

    @Column(name = "extension", length = 20)
    private String extent;

    @Column(name = "estabilidad", length = 20)
    private String stability;

    @Column(name = "diagnostico_final_texto", columnDefinition = "TEXT")
    private String finalDiagnosisText;

    // Getters y setters (generar o añadir manualmente)
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getMeasurementsJson() { return measurementsJson; }
    public void setMeasurementsJson(String measurementsJson) { this.measurementsJson = measurementsJson; }

    public String getDiagnosisBase() { return diagnosisBase; }
    public void setDiagnosisBase(String diagnosisBase) { this.diagnosisBase = diagnosisBase; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getExtent() { return extent; }
    public void setExtent(String extent) { this.extent = extent; }

    public String getStability() { return stability; }
    public void setStability(String stability) { this.stability = stability; }

    public String getFinalDiagnosisText() { return finalDiagnosisText; }
    public void setFinalDiagnosisText(String finalDiagnosisText) { this.finalDiagnosisText = finalDiagnosisText; }
}