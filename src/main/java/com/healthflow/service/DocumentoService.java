package com.healthflow.service;

import com.healthflow.domain.Documento;
import com.healthflow.domain.Patient;
import com.healthflow.repo.DocumentoRepository;
import com.healthflow.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;
    private final String publicBaseUrl;
    private final Path uploadPath;

    public DocumentoService(DocumentoRepository documentoRepository,
                            PatientRepository patientRepository,
                            NotificationService notificationService,
                            @Value("${healthflow.publicBaseUrl:http://localhost:8080}") String publicBaseUrl,
                            @Value("${healthflow.upload-dir:uploads}") String uploadDir) {
        this.documentoRepository = documentoRepository;
        this.patientRepository = patientRepository;
        this.notificationService = notificationService;
        this.publicBaseUrl = publicBaseUrl;
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de subida: " + uploadPath, e);
        }
    }

    // Método para un solo archivo (podría ser usado aún)
    @Transactional
    public Documento uploadDocument(UUID patientId, MultipartFile file, String description,
                                    UUID appointmentId, int expirationDays) throws IOException {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) extension = originalFilename.substring(dotIndex);
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath);

        Documento doc = new Documento();
        doc.setPatient(patient);
        doc.setFileName(originalFilename);
        doc.setFilePath(targetPath.toString());
        doc.setMimeType(file.getContentType());
        doc.setSize(file.getSize());
        doc.setDescription(description);
        doc.setToken(UUID.randomUUID());
        doc.setExpirationDate(OffsetDateTime.now().plus(expirationDays, ChronoUnit.DAYS));

        Documento saved = documentoRepository.save(doc);

        String downloadUrl = publicBaseUrl + "/api/public/documentos/" + saved.getToken();
        notificationService.sendDocumentLinkEmail(
                patient.getEmail(),
                patient.getFirstName() + " " + patient.getLastName(),
                saved.getFileName(),
                downloadUrl,
                saved.getExpirationDate().toLocalDate()
        );

        return saved;
    }

    /**
     * Método original (sobrecarga) que mantiene compatibilidad con llamadas existentes.
     * Por defecto, asigna origen = "MEDICO" y tipoDocumento = null.
     */
    @Transactional
    public List<Documento> uploadMultipleDocuments(UUID patientId, List<MultipartFile> files,
                                                   String description, UUID appointmentId,
                                                   int expirationDays) throws IOException {
        return uploadMultipleDocuments(patientId, files, description, appointmentId, expirationDays, "MEDICO", null, null);
    }

    /**
     * Método completo con parámetros origen y tipoDocumento.
     * Sube múltiples archivos, los asocia a un paciente y envía un correo con los enlaces de descarga.
     *
     * @param patientId      ID del paciente
     * @param files          Lista de archivos a subir
     * @param description    Descripción general (se asigna a cada documento)
     * @param appointmentId  ID de la cita (opcional, puede ser null)
     * @param expirationDays Días de validez del enlace
     * @param origen         Origen del documento: "MEDICO" o "PACIENTE"
     * @param tipoDocumento  Tipo de documento: "FORMULA", "REMISION", "LABORATORIO", "RADIOGRAFIA", "OTRO" (puede ser null)
     * @return Lista de documentos guardados
     */
    @Transactional
    public List<Documento> uploadMultipleDocuments(UUID patientId, List<MultipartFile> files,
                                                   String description, UUID appointmentId,
                                                   int expirationDays, String origen,
                                                   String tipoDocumento, UUID professionalId) throws IOException {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));

        if (origen == null || origen.isEmpty()) {
            origen = "MEDICO";
        }

        List<Documento> savedDocs = new ArrayList<>();
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) extension = originalFilename.substring(dotIndex);
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath);

            Documento doc = new Documento();
            doc.setPatient(patient);
            doc.setFileName(originalFilename);
            doc.setFilePath(targetPath.toString());
            doc.setMimeType(file.getContentType());
            doc.setSize(file.getSize());
            doc.setDescription(description != null ? description : originalFilename);
            doc.setToken(UUID.randomUUID());
            doc.setExpirationDate(OffsetDateTime.now().plus(expirationDays, ChronoUnit.DAYS));
            doc.setOrigen(origen);
            doc.setTipoDocumento(tipoDocumento);
            doc.setProfessionalId(professionalId);
            savedDocs.add(documentoRepository.save(doc));
        }

        // Construir correo con todos los enlaces
        List<String> links = new ArrayList<>();
        for (Documento doc : savedDocs) {
            String link = publicBaseUrl + "/api/public/documentos/" + doc.getToken();
            links.add(doc.getFileName() + ": " + link);
        }
        String bodyLinks = String.join("\n", links);
        String subject = "Documentos disponibles - HealthFlow";
        String body = String.format(
                "Hola %s,\n\nSe han compartido los siguientes documentos:\n\n%s\n\n" +
                        "Cada enlace es válido hasta el %s.\n\nSaludos,\nHealthFlow",
                patient.getFirstName() + " " + patient.getLastName(),
                bodyLinks,
                savedDocs.get(0).getExpirationDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        System.out.println("=== EMAIL (MOCK) ===");
        System.out.println("Para: " + patient.getEmail());
        System.out.println("Asunto: " + subject);
        System.out.println(body);
        System.out.println("====================");

        return savedDocs;
    }

    @Transactional(readOnly = true)
    public List<Documento> getDocumentsByPatient(UUID patientId) {
        return documentoRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public Documento getDocumentById(UUID documentId) {
        return documentoRepository.findById(documentId)
                .orElseThrow(() -> new DomainException("Documento no encontrado"));
    }

    @Transactional(readOnly = true)
    public Documento getByToken(UUID token) {
        Documento doc = documentoRepository.findByToken(token)
                .orElseThrow(() -> new DomainException("Documento no encontrado"));
        if (doc.getExpirationDate().isBefore(OffsetDateTime.now())) {
            throw new DomainException("El enlace ha expirado");
        }
        return doc;
    }

    @Transactional
    public void deleteDocument(UUID documentId, UUID professionalId) {
        Documento doc = documentoRepository.findById(documentId)
                .orElseThrow(() -> new DomainException("Documento no encontrado"));
        try {
            Files.deleteIfExists(Path.of(doc.getFilePath()));
        } catch (IOException e) {
            // log but continue
        }
        documentoRepository.delete(doc);
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void deleteExpiredDocuments() {
        documentoRepository.deleteExpired(OffsetDateTime.now());
    }
}