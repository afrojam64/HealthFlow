package com.healthflow.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class PeriodontogramaRequest {
    private UUID patientId;
    private UUID appointmentId; // opcional
    private LocalDate examDate;
    private String observations;
    private String measurementsJson; // JSON con las mediciones por diente
    private String finalDiagnosisText; // si se envía, el profesional ya eligió diagnóstico

    // Getters y Setters
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getAppointmentId() { return appointmentId; }
    public void setAppointmentId(UUID appointmentId) { this.appointmentId = appointmentId; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getMeasurementsJson() { return measurementsJson; }
    public void setMeasurementsJson(String measurementsJson) { this.measurementsJson = measurementsJson; }

    public String getFinalDiagnosisText() { return finalDiagnosisText; }
    public void setFinalDiagnosisText(String finalDiagnosisText) { this.finalDiagnosisText = finalDiagnosisText; }
}
