package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "documentos")
public class Documento extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Appointment appointment;

    @NotNull
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String fileName;

    @NotNull
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String filePath;

    @Column(name = "tipo_mime", length = 100)
    private String mimeType;

    @Column(name = "tamano")
    private Long size;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "token", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID token;

    @NotNull
    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime expirationDate;

    @Column(name = "origen", nullable = false, length = 20)
    private String origen = "MEDICO";

    @Column(name = "tipo_documento", length = 50)
    private String tipoDocumento;

    // Getters y setters
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getToken() { return token; }
    public void setToken(UUID token) { this.token = token; }

    public OffsetDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(OffsetDateTime expirationDate) { this.expirationDate = expirationDate; }
}