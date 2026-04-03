package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "citas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_citas_prof_fecha", columnNames = {"profesional_id", "fecha_hora"}),
                @UniqueConstraint(name = "uk_citas_token", columnNames = {"token_acceso"})
        }
)
public class Appointment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Professional professional;

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
    protected void onCreate() {
        super.onCreate();
        if (accessToken == null) accessToken = UUID.randomUUID();
    }

    // Getters y Setters
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

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

    public LocalDateTime getLocalDateTime(String zoneId) {
        return getDateTime().atZoneSameInstant(ZoneId.of(zoneId)).toLocalDateTime();
    }
}
