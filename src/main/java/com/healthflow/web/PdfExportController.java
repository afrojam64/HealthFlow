package com.healthflow.web;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.MedicalRecord;
import com.healthflow.domain.Patient;
import com.healthflow.domain.Professional;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.itextpdf.kernel.colors.Color;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/doctor/pacientes")
public class PdfExportController {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final ZoneId zoneId;

    private static final Color PRIMARY_COLOR = new DeviceRgb(42, 127, 110);
    private static final Color GRAY_LIGHT = new DeviceRgb(249, 250, 251);
    private static final Color GRAY_BORDER = new DeviceRgb(229, 231, 235);
    private static final Color TEXT_MUTED = new DeviceRgb(107, 114, 128);

    public PdfExportController(PatientRepository patientRepository,
                               AppointmentRepository appointmentRepository,
                               ProfessionalRepository professionalRepository,
                               UserRepository userRepository,
                               @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
        this.zoneId = ZoneId.of(tz);
    }

    private Professional getCurrentProfessional() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        return professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportHistorialPdf(@PathVariable("id") UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new DomainException("Paciente no encontrado"));
        Professional professional = getCurrentProfessional();

        List<Appointment> citas = appointmentRepository.findByPatientIdOrderByDateTimeDesc(patientId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        try {
            PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // HEADER
            Paragraph headerTitle = new Paragraph("HEALTHFLOW")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(headerTitle);

            Paragraph headerSub = new Paragraph("Gestión Clínica Digital")
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setFontColor(TEXT_MUTED)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(headerSub);
            document.add(new Paragraph(" "));

            // CORREGIDO: usar LocalDateTime para incluir hora
            String fechaGeneracion = LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));
            Paragraph generadoPor = new Paragraph()
                    .add(new Text("Generado: ").setFont(normalFont).setFontColor(TEXT_MUTED).setFontSize(9))
                    .add(new Text(fechaGeneracion).setFont(normalFont).setFontSize(9))
                    .add(new Text("  |  Médico: ").setFont(normalFont).setFontColor(TEXT_MUTED).setFontSize(9))
                    .add(new Text(professional.getFullName() + " (" + professional.getSpecialty() + ")").setFont(normalFont).setFontSize(9));
            document.add(generadoPor);
            document.add(new Paragraph(" "));

            Table patientTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(GRAY_BORDER, 1))
                    .setBackgroundColor(GRAY_LIGHT)
                    .setMarginBottom(10);
            patientTable.addCell(new Cell().add(new Paragraph("Nombre:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getFirstName() + " " + patient.getLastName()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Documento:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getDocNumber()).setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Correo:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getEmail() != null ? patient.getEmail() : "No registrado").setFont(normalFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph("Teléfono:").setFont(boldFont)).setBorder(null));
            patientTable.addCell(new Cell().add(new Paragraph(patient.getPhone() != null ? patient.getPhone() : "No registrado").setFont(normalFont)).setBorder(null));
            document.add(patientTable);
            document.add(new Paragraph(" "));

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int consultaNum = 1;

            for (Appointment cita : citas) {
                MedicalRecord mr = cita.getMedicalRecord();

                SolidLine solidLine = new SolidLine(1f);
                solidLine.setColor(PRIMARY_COLOR);
                LineSeparator ls = new LineSeparator(solidLine);
                document.add(ls);
                document.add(new Paragraph(" "));

                Paragraph consultaTitle = new Paragraph("CONSULTA #" + consultaNum++)
                        .setFont(boldFont)
                        .setFontSize(14)
                        .setFontColor(PRIMARY_COLOR)
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(consultaTitle);
                document.add(new Paragraph(" "));

                String fechaStr = cita.getDateTime().atZoneSameInstant(zoneId).format(dateFormatter);
                Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                        .setWidth(UnitValue.createPercentValue(100));
                infoTable.addCell(new Cell().add(new Paragraph("📅 Fecha: " + fechaStr).setFont(normalFont)).setBorder(null));
                infoTable.addCell(new Cell().add(new Paragraph("Estado: " + cita.getStatus().name()).setFont(normalFont)).setBorder(null));
                document.add(infoTable);
                document.add(new Paragraph(" "));

                if (mr == null) {
                    document.add(new Paragraph("Sin registro clínico asociado.").setFont(normalFont).setFontColor(TEXT_MUTED));
                    document.add(new Paragraph(" "));
                    continue;
                }

                if (mr.getReason() != null && !mr.getReason().isEmpty()) {
                    Div motivoDiv = new Div()
                            .setBorder(new SolidBorder(GRAY_BORDER, 1))
                            .setBackgroundColor(GRAY_LIGHT)
                            .setPadding(8)
                            .setMarginBottom(8);
                    motivoDiv.add(new Paragraph("🔍 Motivo de consulta").setFont(boldFont).setFontSize(10));
                    motivoDiv.add(new Paragraph(mr.getReason()).setFont(normalFont).setFontSize(9));
                    document.add(motivoDiv);
                }

                if (mr.getMainDiagnosis() != null && !mr.getMainDiagnosis().isEmpty()) {
                    document.add(new Paragraph("📌 Diagnóstico principal (CIE-10): " + mr.getMainDiagnosis()).setFont(boldFont).setFontSize(10));
                }
                if (mr.getRelatedDiagnosis1() != null && !mr.getRelatedDiagnosis1().isEmpty()) {
                    document.add(new Paragraph("   Relacionado 1: " + mr.getRelatedDiagnosis1()).setFont(normalFont).setFontSize(9));
                }
                if (mr.getRelatedDiagnosis2() != null && !mr.getRelatedDiagnosis2().isEmpty()) {
                    document.add(new Paragraph("   Relacionado 2: " + mr.getRelatedDiagnosis2()).setFont(normalFont).setFontSize(9));
                }
                if (mr.getComplicationDiagnosis() != null && !mr.getComplicationDiagnosis().isEmpty()) {
                    document.add(new Paragraph("   Complicación: " + mr.getComplicationDiagnosis()).setFont(normalFont).setFontSize(9));
                }

                if (mr.getEvolution() != null && !mr.getEvolution().isEmpty()) {
                    document.add(new Paragraph("📈 Evolución").setFont(boldFont).setFontSize(10));
                    document.add(new Paragraph(mr.getEvolution()).setFont(normalFont).setFontSize(9));
                }

                if (mr.getPrescription() != null && !mr.getPrescription().isEmpty()) {
                    document.add(new Paragraph("💊 Prescripción / Fórmula médica").setFont(boldFont).setFontSize(10));
                    document.add(new Paragraph(mr.getPrescription()).setFont(normalFont).setFontSize(9));
                }

                if (mr.getFinalidadConsulta() != null || mr.getCausaExterna() != null ||
                        mr.getCodigoCups() != null || mr.getValorServicio() != null) {
                    Div ripsDiv = new Div()
                            .setBorder(new SolidBorder(GRAY_BORDER, 1))
                            .setPadding(8)
                            .setMarginTop(8)
                            .setMarginBottom(8);
                    ripsDiv.add(new Paragraph("📋 Información RIPS").setFont(boldFont).setFontSize(10));
                    if (mr.getFinalidadConsulta() != null) {
                        ripsDiv.add(new Paragraph("Finalidad: " + mr.getFinalidadConsulta().getDescripcion()).setFont(normalFont).setFontSize(9));
                    }
                    if (mr.getCausaExterna() != null) {
                        ripsDiv.add(new Paragraph("Causa externa: " + mr.getCausaExterna().getDescripcion()).setFont(normalFont).setFontSize(9));
                    }
                    if (mr.getCodigoCups() != null && !mr.getCodigoCups().isEmpty()) {
                        ripsDiv.add(new Paragraph("Código CUPS: " + mr.getCodigoCups()).setFont(normalFont).setFontSize(9));
                    }
                    if (mr.getValorServicio() != null || mr.getCuotaModeradora() != null || mr.getCopago() != null) {
                        String valores = "";
                        //if (mr.getValorServicio() != null) valores += "Valor: $" + mr.getValorServicio() + "  ";
                        //if (mr.getCuotaModeradora() != null) valores += "Cuota mod: $" + mr.getCuotaModeradora() + "  ";
                        //if (mr.getCopago() != null) valores += "Copago: $" + mr.getCopago();
                        ripsDiv.add(new Paragraph(valores).setFont(normalFont).setFontSize(9));
                    }
                    document.add(ripsDiv);
                }

                document.add(new Paragraph(" "));
            }

            // FOOTER
            SolidLine footerSolidLine = new SolidLine(1f);
            footerSolidLine.setColor(GRAY_BORDER);
            LineSeparator footerLine = new LineSeparator(footerSolidLine);
            document.add(footerLine);
            Paragraph footer = new Paragraph("Documento generado por HealthFlow - " + LocalDate.now(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(normalFont)
                    .setFontSize(8)
                    .setFontColor(TEXT_MUTED)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "historial_completo_" + patient.getDocNumber() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }
}