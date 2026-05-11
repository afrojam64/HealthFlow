package com.healthflow.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class PeriodontogramaResponse {
    private UUID id;
    private UUID patientId;
    private UUID appointmentId;
    private UUID professionalId;
    private LocalDate examDate;
    private String observations;
    private String measurementsJson;
    private String diagnosisBase;
    private String subcategory;
    private String stage;
    private String grade;
    private String extent;
    private String stability;
    private String finalDiagnosisText;
    private String createdAt;
    private String updatedAt;

    // Constructor con todos los campos
    public PeriodontogramaResponse(UUID id, UUID patientId, UUID appointmentId, UUID professionalId,
                                   LocalDate examDate, String observations, String measurementsJson,
                                   String diagnosisBase, String subcategory, String stage, String grade,
                                   String extent, String stability, String finalDiagnosisText,
                                   String createdAt, String updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.professionalId = professionalId;
        this.examDate = examDate;
        this.observations = observations;
        this.measurementsJson = measurementsJson;
        this.diagnosisBase = diagnosisBase;
        this.subcategory = subcategory;
        this.stage = stage;
        this.grade = grade;
        this.extent = extent;
        this.stability = stability;
        this.finalDiagnosisText = finalDiagnosisText;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters (puedes agregar setters si los necesitas)
    public UUID getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public UUID getAppointmentId() { return appointmentId; }
    public UUID getProfessionalId() { return professionalId; }
    public LocalDate getExamDate() { return examDate; }
    public String getObservations() { return observations; }
    public String getMeasurementsJson() { return measurementsJson; }
    public String getDiagnosisBase() { return diagnosisBase; }
    public String getSubcategory() { return subcategory; }
    public String getStage() { return stage; }
    public String getGrade() { return grade; }
    public String getExtent() { return extent; }
    public String getStability() { return stability; }
    public String getFinalDiagnosisText() { return finalDiagnosisText; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}