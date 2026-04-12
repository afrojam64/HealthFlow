package com.healthflow.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthflow.domain.*;
import com.healthflow.repo.*;
import com.healthflow.service.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/citas")
public class DoctorAppointmentAttentionController {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final CatalogoFinalidadConsultaRepository finalidadRepo;
    private final CatalogoCausaExternaRepository causaExternaRepo;
    private final DiagnosticoCIE10Repository diagnosticoRepo;
    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final ZoneId zoneId;
    private final QrCodeService qrCodeService;
    private final DocumentoFirmadoRepository documentoFirmadoRepository;
    private final String publicBaseUrl;
    private final RemisionService remisionService;
    private final EspecialidadRepository especialidadRepository;
    private final DocumentoService documentoService;

    public DoctorAppointmentAttentionController(AppointmentRepository appointmentRepository,
                                                MedicalRecordService medicalRecordService,
                                                MedicalRecordRepository medicalRecordRepository,
                                                CatalogoFinalidadConsultaRepository finalidadRepo,
                                                CatalogoCausaExternaRepository causaExternaRepo,
                                                DiagnosticoCIE10Repository diagnosticoRepo,
                                                UserRepository userRepository,
                                                ProfessionalRepository professionalRepository,
                                                @Value("${healthflow.timezone:America/Bogota}") String timezone,
                                                QrCodeService qrCodeService,
                                                DocumentoFirmadoRepository documentoFirmadoRepository,
                                                @Value("${healthflow.publicBaseUrl:http://localhost:8080}") String publicBaseUrl, RemisionService remisionService, EspecialidadRepository especialidadRepository, DocumentoService documentoService) {
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordService = medicalRecordService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.finalidadRepo = finalidadRepo;
        this.causaExternaRepo = causaExternaRepo;
        this.diagnosticoRepo = diagnosticoRepo;
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.zoneId = ZoneId.of(timezone);
        this.qrCodeService = qrCodeService;
        this.documentoFirmadoRepository = documentoFirmadoRepository;
        this.publicBaseUrl = publicBaseUrl;
        this.remisionService = remisionService;
        this.especialidadRepository = especialidadRepository;
        this.documentoService = documentoService;
    }

    private Professional getCurrentProfessional() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        return professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
    }

    @GetMapping("/{id}/atender")
    public String showAttentionForm(@PathVariable("id") UUID appointmentId, Model model) {
        MedicalRecord currentRecord = medicalRecordService.getOrCreateForAppointment(appointmentId);
        Appointment appointment = currentRecord.getAppointment();

        List<MedicalRecord> previousRecords = medicalRecordRepository
                .findByAppointmentPatientIdOrderByAppointmentDateTimeDesc(appointment.getPatient().getId())
                .stream()
                .filter(rec -> !rec.getId().equals(currentRecord.getId()))
                .toList();

        List<CatalogoFinalidadConsulta> finalidades = finalidadRepo.findAll();
        List<CatalogoCausaExterna> causas = causaExternaRepo.findAll();
        List<DiagnosticoCIE10> diagnosticos = diagnosticoRepo.findAll();

        model.addAttribute("appointment", appointment);
        model.addAttribute("patient", appointment.getPatient());
        model.addAttribute("medicalRecord", currentRecord);
        model.addAttribute("previousRecords", previousRecords);
        model.addAttribute("finalidades", finalidades);
        model.addAttribute("causas", causas);
        model.addAttribute("diagnosticos", diagnosticos);
        model.addAttribute("especialidades", especialidadRepository.findAll());
        model.addAttribute("title", "Atención Clínica");

        model.addAttribute("contenido", "doctor/atencion");
        return "fragments/layout";
    }

    /**
     * Guarda o actualiza la historia clínica de una cita.
     * Recibe los datos del formulario de atención clínica, incluyendo los nuevos campos
     * enfermedad actual, examen físico y concepto, y delega el guardado al servicio.
     * Si la acción es "pdf", redirige al endpoint de generación de PDF después de guardar.
     */
    @PostMapping("/{id}/guardar")
    public String saveClinicalNote(@PathVariable("id") UUID appointmentId,
                                   @RequestParam(name = "reason", required = false) String reason,
                                   @RequestParam(name = "enfermedadActual", required = false) String enfermedadActual,
                                   @RequestParam(name = "examenFisico", required = false) String examenFisico,
                                   @RequestParam(name = "concepto", required = false) String concepto,
                                   @RequestParam(name = "prescription", required = false) String prescription,
                                   @RequestParam(name = "prescriptionJson", required = false) String prescriptionJson,
                                   @RequestParam(name = "mainDiagnosis", required = false) String mainDiagnosis,
                                   @RequestParam(name = "finalidadId", required = false) Long finalidadId,
                                   @RequestParam(name = "causaExternaId", required = false) Long causaExternaId,
                                   @RequestParam(name = "valorServicio", required = false) BigDecimal valorServicio,
                                   @RequestParam(name = "cuotaModeradora", required = false) BigDecimal cuotaModeradora,
                                   @RequestParam(name = "copago", required = false) BigDecimal copago,
                                   @RequestParam(name = "codigoCups", required = false) String codigoCups,
                                   @RequestParam(name = "relatedDiagnosis1", required = false) String relatedDiagnosis1,
                                   @RequestParam(name = "relatedDiagnosis2", required = false) String relatedDiagnosis2,
                                   @RequestParam(name = "complicationDiagnosis", required = false) String complicationDiagnosis,
                                   @RequestParam(name = "accion", required = false) String accion,
                                   RedirectAttributes redirectAttributes) {

        System.out.println("=== saveClinicalNote ===");
        System.out.println("accion: " + accion);
        System.out.println("reason: " + reason);
        System.out.println("prescriptionJson: " + prescriptionJson);

        try {
            medicalRecordService.saveMedicalRecord(appointmentId, reason, enfermedadActual, examenFisico, concepto,
                    prescription, prescriptionJson, mainDiagnosis, finalidadId, causaExternaId, valorServicio, cuotaModeradora,
                    copago, codigoCups, relatedDiagnosis1, relatedDiagnosis2, complicationDiagnosis);

            if ("finalizar".equals(accion)) {
                medicalRecordService.markAsAttendedAndLock(appointmentId);
                redirectAttributes.addFlashAttribute("successMessage", "Consulta finalizada y cerrada correctamente.");
                return "redirect:/dashboard";
            } else if ("pdf".equals(accion)) {
                return "redirect:/doctor/citas/" + appointmentId + "/prescription-pdf";
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Borrador guardado correctamente.");
                return "redirect:/doctor/citas/" + appointmentId + "/atender";
            }
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/doctor/citas/" + appointmentId + "/atender";
        }
    }

    /**
     * Genera y descarga el PDF de la fórmula médica a partir de los datos ya guardados en la base de datos.
     * Utiliza iText 7 para construir el documento.
     * Si no hay datos, retorna un error 400 con mensaje JSON (no redirige a login).
     */
    @GetMapping("/{id}/prescription-pdf")
    public ResponseEntity<?> generatePrescriptionPdf(@PathVariable("id") UUID appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new DomainException("Cita no encontrada"));
            MedicalRecord record = appointment.getMedicalRecord();
            if (record == null || record.getPrescripcionJson() == null) {
                return ResponseEntity.badRequest().body("{\"error\":\"No hay fórmula médica guardada para esta cita.\"}");
            }

            Patient patient = appointment.getPatient();
            Professional professional = getCurrentProfessional();

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> medicamentos = mapper.readValue(record.getPrescripcionJson(),
                    new com.fasterxml.jackson.core.type.TypeReference<>() {});

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // Encabezado
            Paragraph headerTitle = new Paragraph("HEALTHFLOW")
                    .setFont(boldFont).setFontSize(20)
                    .setFontColor(new DeviceRgb(42, 127, 110))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(headerTitle);
            Paragraph headerSub = new Paragraph("Fórmula Médica")
                    .setFont(normalFont).setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(headerSub);
            document.add(new Paragraph(" "));

            // Fecha de generación
            String fechaGeneracion = LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph fecha = new Paragraph("Generado: " + fechaGeneracion)
                    .setFont(normalFont).setFontSize(9)
                    .setFontColor(new DeviceRgb(107, 114, 128));
            document.add(fecha);
            document.add(new Paragraph(" "));

            // Datos del paciente y médico
            Table patientTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1))
                    .setBackgroundColor(new DeviceRgb(249, 250, 251))
                    .setMarginBottom(10);
            patientTable.addCell(new Cell().add(new Paragraph("Paciente:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getFirstName() + " " + patient.getLastName()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Documento:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getDocNumber()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Médico:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(professional.getFullName()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Especialidad:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(professional.getSpecialty()).setFont(normalFont)).setBorder(null));
            document.add(patientTable);
            document.add(new Paragraph(" "));

            // Tabla de medicamentos
            Paragraph medTitle = new Paragraph("Medicamentos recetados")
                    .setFont(boldFont).setFontSize(12)
                    .setFontColor(new DeviceRgb(42, 127, 110));
            document.add(medTitle);
            document.add(new Paragraph(" "));

            Table medTable = new Table(UnitValue.createPercentArray(new float[]{40, 25, 25, 10}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Medicamento").setFont(boldFont)));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Dosis").setFont(boldFont)));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Frecuencia").setFont(boldFont)));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setFont(boldFont)));

            for (Map<String, String> med : medicamentos) {
                medTable.addCell(new Cell().add(new Paragraph(med.get("nombre")).setFont(normalFont)));
                medTable.addCell(new Cell().add(new Paragraph(med.get("dosis")).setFont(normalFont)));
                medTable.addCell(new Cell().add(new Paragraph(med.get("frecuencia")).setFont(normalFont)));
                medTable.addCell(new Cell().add(new Paragraph(med.getOrDefault("cantidad", "1")).setFont(normalFont)));
            }
            document.add(medTable);
            document.add(new Paragraph(" "));

            // Indicaciones adicionales (si existen)
            if (record.getPrescription() != null && !record.getPrescription().isEmpty()) {
                Paragraph indicacionesTitle = new Paragraph("Indicaciones adicionales")
                        .setFont(boldFont).setFontSize(11);
                document.add(indicacionesTitle);
                Paragraph indicaciones = new Paragraph(record.getPrescription())
                        .setFont(normalFont).setFontSize(10);
                document.add(indicaciones);
                document.add(new Paragraph(" "));
            }

            // Firma
            Paragraph firmaTitle = new Paragraph("Firma del médico")
                    .setFont(boldFont).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(firmaTitle);
            Paragraph firmaLinea = new Paragraph("_________________________")
                    .setFont(normalFont).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(firmaLinea);
            Paragraph nombreMedico = new Paragraph(professional.getFullName())
                    .setFont(normalFont).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(nombreMedico);

            // Footer
            SolidLine footerLine = new SolidLine(1f);
            footerLine.setColor(new DeviceRgb(229, 231, 235));
            LineSeparator ls = new LineSeparator(footerLine);
            document.add(ls);
            Paragraph footer = new Paragraph("Documento generado por HealthFlow - " + LocalDate.now(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(normalFont).setFontSize(8)
                    .setFontColor(new DeviceRgb(107, 114, 128))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "prescripcion_" + patient.getDocNumber() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (DomainException e) {
            // Error de negocio (cita no encontrada, etc.)
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\":\"Error interno al generar el PDF: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Genera y descarga el PDF de la fórmula médica a partir del JSON de medicamentos enviado en el cuerpo de la petición.
     * No requiere guardar previamente en la base de datos. Ruta exenta de CSRF.
     */
    @PostMapping("/{id}/prescription-pdf")
    public ResponseEntity<byte[]> generatePrescriptionPdfFromJson(@PathVariable("id") UUID appointmentId,
                                                                  @RequestBody Map<String, Object> payload) throws Exception {
        // Obtener la cita, paciente y profesional
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));
        Patient patient = appointment.getPatient();
        Professional professional = getCurrentProfessional();

        // Obtener medicamentos del payload
        @SuppressWarnings("unchecked")
        List<Map<String, String>> medicamentos = (List<Map<String, String>>) payload.get("medicamentos");
        if (medicamentos == null || medicamentos.isEmpty()) {
            throw new DomainException("No hay medicamentos en la fórmula.");
        }

        // Generar token único para verificación
        UUID token = UUID.randomUUID();
        DocumentoFirmado docFirmado = new DocumentoFirmado();
        docFirmado.setToken(token);
        docFirmado.setTipoDocumento("FORMULA_MEDICA");
        docFirmado.setReferenciaId(appointmentId);
        docFirmado.setFechaCreacion(OffsetDateTime.now(zoneId));
        String metadata = String.format("{\"paciente\":\"%s\", \"medico\":\"%s\", \"paciente_doc\":\"%s\"}",
                patient.getFirstName() + " " + patient.getLastName(),
                professional.getFullName(),
                patient.getDocNumber());
        docFirmado.setMetadata(metadata);
        documentoFirmadoRepository.save(docFirmado);

        // URL de verificación
        String verificationUrl = publicBaseUrl + "/public/verify/document?token=" + token;

        // Generar QR
        byte[] qrImage = qrCodeService.generateQrCode(verificationUrl, 150, 150);

        // Cargar firma si existe
        byte[] firmaImage = null;
        if (professional.getFirmaUrl() != null) {
            Path firmaPath = Path.of(professional.getFirmaUrl());
            if (Files.exists(firmaPath)) {
                firmaImage = Files.readAllBytes(firmaPath);
            }
        }

        // Generar PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        try {
            PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // Encabezado
            Paragraph headerTitle = new Paragraph("HEALTHFLOW")
                    .setFont(boldFont).setFontSize(20)
                    .setFontColor(new DeviceRgb(42, 127, 110))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(headerTitle);
            Paragraph headerSub = new Paragraph("Fórmula Médica")
                    .setFont(normalFont).setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(headerSub);
            document.add(new Paragraph(" "));

            // Fecha
            String fechaGeneracion = LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph fecha = new Paragraph("Generado: " + fechaGeneracion)
                    .setFont(normalFont).setFontSize(9)
                    .setFontColor(new DeviceRgb(107, 114, 128));
            document.add(fecha);
            document.add(new Paragraph(" "));

            // Datos paciente y médico
            Table patientTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1))
                    .setBackgroundColor(new DeviceRgb(249, 250, 251))
                    .setMarginBottom(10);
            patientTable.addCell(new Cell().add(new Paragraph("Paciente:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getFirstName() + " " + patient.getLastName()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Documento:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getDocNumber()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Médico:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(professional.getFullName()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Especialidad:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(professional.getSpecialty()).setFont(normalFont)).setBorder(null));
            document.add(patientTable);
            document.add(new Paragraph(" "));

            // Tabla de medicamentos
            Paragraph medTitle = new Paragraph("Medicamentos recetados")
                    .setFont(boldFont).setFontSize(12)
                    .setFontColor(new DeviceRgb(42, 127, 110));
            document.add(medTitle);
            document.add(new Paragraph(" "));

            Table medTable = new Table(UnitValue.createPercentArray(new float[]{40, 25, 25, 10}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Medicamento").setFont(boldFont)));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Dosis").setFont(boldFont)));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Frecuencia").setFont(boldFont)));
            medTable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setFont(boldFont)));

            for (Map<String, String> med : medicamentos) {
                medTable.addCell(new Cell().add(new Paragraph(med.get("nombre")).setFont(normalFont)));
                medTable.addCell(new Cell().add(new Paragraph(med.get("dosis")).setFont(normalFont)));
                medTable.addCell(new Cell().add(new Paragraph(med.get("frecuencia")).setFont(normalFont)));
                medTable.addCell(new Cell().add(new Paragraph(med.getOrDefault("cantidad", "1")).setFont(normalFont)));
            }
            document.add(medTable);
            document.add(new Paragraph(" "));

            // Indicaciones adicionales
            String indicaciones = (String) payload.get("indicaciones");
            if (indicaciones != null && !indicaciones.isEmpty()) {
                Paragraph indicacionesTitle = new Paragraph("Indicaciones adicionales")
                        .setFont(boldFont).setFontSize(11);
                document.add(indicacionesTitle);
                Paragraph indicacionesPara = new Paragraph(indicaciones)
                        .setFont(normalFont).setFontSize(10);
                document.add(indicacionesPara);
                document.add(new Paragraph(" "));
            }

            // Firma
            Paragraph firmaTitle = new Paragraph("Firma del médico")
                    .setFont(boldFont).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(firmaTitle);
            if (firmaImage != null) {
                Image firmaImg = new Image(ImageDataFactory.create(firmaImage));
                firmaImg.setWidth(120);
                firmaImg.setHeight(60);
                firmaImg.setTextAlignment(TextAlignment.CENTER);
                document.add(firmaImg);
            } else {
                Paragraph firmaLinea = new Paragraph("_________________________")
                        .setFont(normalFont).setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(firmaLinea);
            }
            Paragraph nombreMedico = new Paragraph(professional.getFullName())
                    .setFont(normalFont).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(nombreMedico);

            // Código QR
            if (qrImage != null) {
                Image qrImg = new Image(ImageDataFactory.create(qrImage));
                qrImg.setWidth(80);
                qrImg.setHeight(80);
                qrImg.setTextAlignment(TextAlignment.RIGHT);
                document.add(qrImg);
                Paragraph qrNote = new Paragraph("Escanee para verificar autenticidad")
                        .setFont(normalFont).setFontSize(7)
                        .setFontColor(new DeviceRgb(107, 114, 128))
                        .setTextAlignment(TextAlignment.RIGHT);
                document.add(qrNote);
            }

            // Footer
            SolidLine footerLine = new SolidLine(1f);
            footerLine.setColor(new DeviceRgb(229, 231, 235));
            LineSeparator ls = new LineSeparator(footerLine);
            document.add(ls);
            Paragraph footer = new Paragraph("Documento generado por HealthFlow - " + LocalDate.now(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(normalFont).setFontSize(8)
                    .setFontColor(new DeviceRgb(107, 114, 128))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "prescripcion_" + patient.getDocNumber() + ".pdf");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar PDF de prescripción", e);
        }
    }

    @PostMapping("/{id}/remision")
    public ResponseEntity<byte[]> generarRemision(@PathVariable("id") UUID appointmentId,
                                                  @RequestBody Map<String, Object> payload) throws Exception {
        String motivo = (String) payload.get("motivo");
        String especialidad = (String) payload.get("especialidad");
        String prioridad = (String) payload.get("prioridad");
        @SuppressWarnings("unchecked")
        Map<String, String> snapshotData = (Map<String, String>) payload.get("snapshotData");
        if (snapshotData == null) {
            throw new DomainException("No se pudo obtener el resumen clínico.");
        }
        Professional professional = getCurrentProfessional();
        String firmaUrl = professional.getFirmaUrl();
        byte[] pdfBytes = remisionService.generarRemision(appointmentId, motivo, especialidad, prioridad, snapshotData, firmaUrl);

        // Guardar automáticamente como documento (para envío de correo)
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));
        String fileName = "remision_" + appointmentId + ".pdf";
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("remision_", ".pdf");
        java.nio.file.Files.write(tempFile, pdfBytes);
        MultipartFile multipartFile = new MockMultipartFile("file", fileName, "application/pdf", pdfBytes);
        documentoService.uploadDocument(appointment.getPatient().getId(), multipartFile,
                "Remisión a " + especialidad + " - " + motivo.substring(0, Math.min(motivo.length(), 50)), null, 30);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}