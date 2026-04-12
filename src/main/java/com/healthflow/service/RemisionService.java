package com.healthflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthflow.domain.*;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.RemisionRepository;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
public class RemisionService {

    private final RemisionRepository remisionRepository;
    private final AppointmentRepository appointmentRepository;
    private final QrCodeService qrCodeService;
    private final String publicBaseUrl;
    private final ZoneId zoneId;

    public RemisionService(RemisionRepository remisionRepository,
                           AppointmentRepository appointmentRepository,
                           QrCodeService qrCodeService,
                           @Value("${healthflow.publicBaseUrl:http://localhost:8080}") String publicBaseUrl,
                           @Value("${healthflow.timezone:America/Bogota}") String timezone) {
        this.remisionRepository = remisionRepository;
        this.appointmentRepository = appointmentRepository;
        this.qrCodeService = qrCodeService;
        this.publicBaseUrl = publicBaseUrl;
        this.zoneId = ZoneId.of(timezone);
    }

    @Transactional
    public byte[] generarRemision(UUID appointmentId, String motivo, String especialidad, String prioridad,
                                  Map<String, String> snapshotData, String firmaUrl) throws Exception {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));
        Patient patient = appointment.getPatient();
        Professional professional = appointment.getProfessional();

        // Crear token y guardar metadatos
        UUID token = UUID.randomUUID();
        ObjectMapper mapper = new ObjectMapper();
        String snapshotJson = mapper.writeValueAsString(snapshotData);

        Remision remision = new Remision();
        remision.setCitaId(appointmentId);
        remision.setToken(token);
        remision.setMotivo(motivo);
        remision.setEspecialidad(especialidad);
        remision.setPrioridad(prioridad);
        remision.setSnapshot(snapshotJson);
        remision.setFechaCreacion(LocalDateTime.now(zoneId).atZone(zoneId).toOffsetDateTime());
        remisionRepository.save(remision);

        // Generar PDF
        return generarPdfRemision(patient, professional, motivo, especialidad, prioridad, snapshotData, firmaUrl, token);
    }

    private byte[] generarPdfRemision(Patient patient, Professional professional, String motivo,
                                      String especialidad, String prioridad, Map<String, String> snapshotData,
                                      String firmaUrl, UUID token) throws Exception {
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
        Paragraph headerSub = new Paragraph("REMISIÓN MÉDICA")
                .setFont(boldFont).setFontSize(14)
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

        // Datos del paciente y médico
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1))
                .setBackgroundColor(new DeviceRgb(249, 250, 251))
                .setMarginBottom(10);
        infoTable.addCell(new Cell().add(new Paragraph("Paciente:").setFont(boldFont)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph(patient.getFirstName() + " " + patient.getLastName()).setFont(normalFont)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph("Documento:").setFont(boldFont)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph(patient.getDocNumber()).setFont(normalFont)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph("Médico remitente:").setFont(boldFont)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph(professional.getFullName()).setFont(normalFont)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph("Especialidad:").setFont(boldFont)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph(professional.getSpecialty()).setFont(normalFont)).setBorder(null));
        document.add(infoTable);
        document.add(new Paragraph(" "));

        // Datos de la remisión
        Table remisionTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);
        remisionTable.addCell(new Cell().add(new Paragraph("Especialidad destino:").setFont(boldFont)).setBorder(null));
        remisionTable.addCell(new Cell().add(new Paragraph(especialidad).setFont(normalFont)).setBorder(null));
        remisionTable.addCell(new Cell().add(new Paragraph("Prioridad:").setFont(boldFont)).setBorder(null));
        remisionTable.addCell(new Cell().add(new Paragraph(prioridad).setFont(normalFont)).setBorder(null));
        remisionTable.addCell(new Cell().add(new Paragraph("Motivo de remisión:").setFont(boldFont)).setBorder(null));
        remisionTable.addCell(new Cell().add(new Paragraph(motivo).setFont(normalFont)).setBorder(null));
        document.add(remisionTable);
        document.add(new Paragraph(" "));

        // Resumen clínico
        Paragraph resumenTitle = new Paragraph("Resumen clínico")
                .setFont(boldFont).setFontSize(12)
                .setFontColor(new DeviceRgb(42, 127, 110));
        document.add(resumenTitle);
        document.add(new Paragraph(" "));

        // Enfermedad actual
        if (snapshotData.containsKey("enfermedadActual") && snapshotData.get("enfermedadActual") != null && !snapshotData.get("enfermedadActual").isEmpty()) {
            document.add(new Paragraph("Enfermedad actual:").setFont(boldFont));
            document.add(new Paragraph(snapshotData.get("enfermedadActual")).setFont(normalFont));
            document.add(new Paragraph(" "));
        }

        // Examen físico
        if (snapshotData.containsKey("examenFisico") && snapshotData.get("examenFisico") != null && !snapshotData.get("examenFisico").isEmpty()) {
            document.add(new Paragraph("Examen físico:").setFont(boldFont));
            document.add(new Paragraph(snapshotData.get("examenFisico")).setFont(normalFont));
            document.add(new Paragraph(" "));
        }

        // Diagnóstico principal
        if (snapshotData.containsKey("mainDiagnosis") && snapshotData.get("mainDiagnosis") != null && !snapshotData.get("mainDiagnosis").isEmpty()) {
            document.add(new Paragraph("Diagnóstico principal (CIE-10):").setFont(boldFont));
            document.add(new Paragraph(snapshotData.get("mainDiagnosis")).setFont(normalFont));
            document.add(new Paragraph(" "));
        }

        // Medicamentos formulados
        String medicamentos = snapshotData.get("medicamentos");
        if (medicamentos != null && !medicamentos.isEmpty() && !medicamentos.equals("[]")) {
            document.add(new Paragraph("Medicamentos formulados:").setFont(boldFont));
            document.add(new Paragraph(medicamentos).setFont(normalFont));
            document.add(new Paragraph(" "));
        }

        // Concepto
        if (snapshotData.containsKey("concepto") && snapshotData.get("concepto") != null && !snapshotData.get("concepto").isEmpty()) {
            document.add(new Paragraph("Concepto / plan:").setFont(boldFont));
            document.add(new Paragraph(snapshotData.get("concepto")).setFont(normalFont));
            document.add(new Paragraph(" "));
        }

        // Firma
        Paragraph firmaTitle = new Paragraph("Firma del médico")
                .setFont(boldFont).setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(firmaTitle);
        if (firmaUrl != null && Files.exists(Path.of(firmaUrl))) {
            com.itextpdf.layout.element.Image firmaImg = new com.itextpdf.layout.element.Image(com.itextpdf.io.image.ImageDataFactory.create(Files.readAllBytes(Path.of(firmaUrl))));
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
        String verificationUrl = publicBaseUrl + "/public/verify/remision?token=" + token;
        byte[] qrImage = qrCodeService.generateQrCode(verificationUrl, 100, 100);
        if (qrImage != null) {
            com.itextpdf.layout.element.Image qrImg = new com.itextpdf.layout.element.Image(com.itextpdf.io.image.ImageDataFactory.create(qrImage));
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
        return baos.toByteArray();
    }
}