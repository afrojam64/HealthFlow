package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "citas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_citas_prof_fecha", columnNames = {"profesional_id", "fecha_hora"}),
                @UniqueConstraint(name = "uk_citas_token", columnNames = {"token_acceso"})
        }
)
public class Appointment {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotNull
    @Column(name = "paciente_id", nullable = false, columnDefinition = "uuid")
    private UUID patientId;

    @NotNull
    @Column(name = "profesional_id", nullable = false, columnDefinition = "uuid")
    private UUID professionalId;

    @NotNull
    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime dateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDIENTE;

    @Column(name = "token_acceso", columnDefinition = "uuid", unique = true)
    private UUID accessToken;

    // ✅ NUEVO: idempotencia recordatorios
    @Column(name = "recordatorio_enviado_at")
    private OffsetDateTime reminderSentAt;

    // Relación con Historia Clínica (opcional, porque puede no haberse atendido aún)
    @OneToOne(mappedBy = "appointment", fetch = FetchType.LAZY)
    private MedicalRecord medicalRecord;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (accessToken == null) accessToken = UUID.randomUUID();
    }

    public UUID getId() { return id; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }

    public OffsetDateTime getDateTime() { return dateTime; }
    public void setDateTime(OffsetDateTime dateTime) { this.dateTime = dateTime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public UUID getAccessToken() { return accessToken; }
    public void setAccessToken(UUID accessToken) { this.accessToken = accessToken; }

    public OffsetDateTime getReminderSentAt() { return reminderSentAt; }
    public void setReminderSentAt(OffsetDateTime reminderSentAt) { this.reminderSentAt = reminderSentAt; }

    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
}