package com.healthflow.service;

import com.healthflow.api.dto.OrdenExamenRequestDTO;
import com.healthflow.api.dto.OrdenExamenResponseDTO;
import com.healthflow.api.dto.OrdenExamenDetalleResponseDTO;
import com.healthflow.api.dto.SolicitudExamenDTO;
import com.healthflow.domain.*;
import com.healthflow.repo.*;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrdenExamenService {

    private final OrdenExamenRepository ordenExamenRepository;
    private final OrdenExamenDetalleRepository ordenExamenDetalleRepository;
    private final CatalogoExamenRepository catalogoExamenRepository;
    private final PerfilClinicoRepository perfilClinicoRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionalRepository;
    private final DocumentoService documentoService;

    public OrdenExamenService(OrdenExamenRepository ordenExamenRepository,
                              OrdenExamenDetalleRepository ordenExamenDetalleRepository,
                              CatalogoExamenRepository catalogoExamenRepository,
                              PerfilClinicoRepository perfilClinicoRepository,
                              AppointmentRepository appointmentRepository,
                              ProfessionalRepository professionalRepository,
                              DocumentoService documentoService) {
        this.ordenExamenRepository = ordenExamenRepository;
        this.ordenExamenDetalleRepository = ordenExamenDetalleRepository;
        this.catalogoExamenRepository = catalogoExamenRepository;
        this.perfilClinicoRepository = perfilClinicoRepository;
        this.appointmentRepository = appointmentRepository;
        this.professionalRepository = professionalRepository;
        this.documentoService = documentoService;
    }

    @Transactional
    public OrdenExamenResponseDTO crearOrden(UUID citaId, OrdenExamenRequestDTO request, UUID profesionalId) {
        Appointment cita = appointmentRepository.findById(citaId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));
        Patient paciente = cita.getPatient();
        Professional profesional = professionalRepository.findById(profesionalId)
                .orElseThrow(() -> new DomainException("Profesional no encontrado"));

        OrdenExamen orden = new OrdenExamen();
        orden.setAppointment(cita);
        orden.setPatient(paciente);
        orden.setProfessional(profesional);
        orden.setFechaSolicitud(OffsetDateTime.now());
        orden.setEstado("PENDIENTE");
        orden.setObservacionesGenerales(request.getObservacionesGenerales());
        orden = ordenExamenRepository.save(orden);

        List<OrdenExamenDetalle> detalles = new ArrayList<>();
        for (SolicitudExamenDTO sol : request.getSolicitudes()) {
            if (sol.getPerfilId() != null) {
                PerfilClinico perfil = perfilClinicoRepository.findByIdWithExamenes(sol.getPerfilId())
                        .orElseThrow(() -> new DomainException("Perfil no encontrado"));
                for (CatalogoExamen examen : perfil.getExamenes()) {
                    detalles.add(crearDetalle(orden, examen, sol.getInstrucciones()));
                }
            } else if (sol.getExamenId() != null) {
                CatalogoExamen examen = catalogoExamenRepository.findById(sol.getExamenId())
                        .orElseThrow(() -> new DomainException("Examen no encontrado"));
                detalles.add(crearDetalle(orden, examen, sol.getInstrucciones()));
            } else {
                throw new DomainException("Cada solicitud debe tener examenId o perfilId");
            }
        }
        orden.setDetalles(detalles);
        ordenExamenDetalleRepository.saveAll(detalles);

        byte[] pdfBytes = generarPDF(orden);
        String fileName = "orden_examen_" + orden.getId() + ".pdf";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "application/pdf", pdfBytes);
        try {
            Documento documento = documentoService.uploadDocument(paciente.getId(), mockFile,
                    "Orden de examen - " + orden.getFechaSolicitud().toLocalDate().toString(), null, 30);
            orden.setDocumentoId(documento.getId());
        } catch (IOException e) {
            throw new DomainException("Error al guardar el PDF de la orden de exámenes: " + e.getMessage());
        }
        ordenExamenRepository.save(orden);

        return toResponseDTO(orden);
    }

    private OrdenExamenDetalle crearDetalle(OrdenExamen orden, CatalogoExamen examen, String instrucciones) {
        OrdenExamenDetalle detalle = new OrdenExamenDetalle();
        detalle.setOrdenExamen(orden);
        detalle.setExamen(examen);
        detalle.setCupsCodigo(examen.getCodigoCups());
        detalle.setNombreExamen(examen.getNombre());
        detalle.setInstruccionesEspecificas(instrucciones);
        return detalle;
    }

    private byte[] generarPDF(OrdenExamen orden) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // Encabezado
            Paragraph header = new Paragraph("HEALTHFLOW")
                    .setFont(boldFont).setFontSize(20)
                    .setFontColor(new DeviceRgb(42, 127, 110))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);
            Paragraph subHeader = new Paragraph("ORDEN DE EXÁMENES")
                    .setFont(normalFont).setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(subHeader);
            document.add(new Paragraph(" "));

            // Fecha
            String fechaStr = orden.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            document.add(new Paragraph("Generado: " + fechaStr).setFont(normalFont).setFontSize(9).setFontColor(new DeviceRgb(107, 114, 128)));
            document.add(new Paragraph(" "));

            // Datos del paciente y médico
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1))
                    .setBackgroundColor(new DeviceRgb(249, 250, 251))
                    .setMarginBottom(10);
            addRow(infoTable, "Paciente:", orden.getPatient().getFirstName() + " " + orden.getPatient().getLastName(), boldFont, normalFont);
            addRow(infoTable, "Documento:", orden.getPatient().getDocNumber(), boldFont, normalFont);
            addRow(infoTable, "Médico:", orden.getProfessional().getFullName(), boldFont, normalFont);
            addRow(infoTable, "Especialidad:", orden.getProfessional().getSpecialty(), boldFont, normalFont);
            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Lista de exámenes
            Paragraph examTitle = new Paragraph("Exámenes solicitados")
                    .setFont(boldFont).setFontSize(12)
                    .setFontColor(new DeviceRgb(42, 127, 110));
            document.add(examTitle);
            document.add(new Paragraph(" "));

            Table examTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1));
            examTable.addHeaderCell(new Cell().add(new Paragraph("Examen").setFont(boldFont)));
            examTable.addHeaderCell(new Cell().add(new Paragraph("Instrucciones").setFont(boldFont)));

            for (OrdenExamenDetalle detalle : orden.getDetalles()) {
                examTable.addCell(new Cell().add(new Paragraph(detalle.getNombreExamen()).setFont(normalFont)));
                String instrucciones = detalle.getInstruccionesEspecificas() != null ? detalle.getInstruccionesEspecificas() : "";
                examTable.addCell(new Cell().add(new Paragraph(instrucciones).setFont(normalFont)));
            }
            document.add(examTable);
            document.add(new Paragraph(" "));

            // Observaciones generales
            if (orden.getObservacionesGenerales() != null && !orden.getObservacionesGenerales().isEmpty()) {
                Paragraph obsTitle = new Paragraph("Observaciones generales")
                        .setFont(boldFont).setFontSize(11);
                document.add(obsTitle);
                Paragraph obs = new Paragraph(orden.getObservacionesGenerales())
                        .setFont(normalFont).setFontSize(10);
                document.add(obs);
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
            Paragraph nombreMedico = new Paragraph(orden.getProfessional().getFullName())
                    .setFont(normalFont).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(nombreMedico);

            // Footer
            SolidLine footerLine = new SolidLine(1f);
            footerLine.setColor(new DeviceRgb(229, 231, 235));
            LineSeparator ls = new LineSeparator(footerLine);
            document.add(ls);
            Paragraph footer = new Paragraph("Documento generado por HealthFlow - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(normalFont).setFontSize(8)
                    .setFontColor(new DeviceRgb(107, 114, 128))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new DomainException("Error al generar PDF de orden de exámenes: " + e.getMessage());
        }
    }

    private void addRow(Table table, String label, String value, PdfFont boldFont, PdfFont normalFont) {
        try {
            table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph(value).setFont(normalFont)).setBorder(null));
        } catch (Exception e) {
            // No debería ocurrir, pero en caso de error, lanzamos excepción de dominio
            throw new DomainException("Error al añadir fila a la tabla del PDF: " + e.getMessage());
        }
    }

    private OrdenExamenResponseDTO toResponseDTO(OrdenExamen orden) {
        OrdenExamenResponseDTO dto = new OrdenExamenResponseDTO();
        dto.setId(orden.getId());
        dto.setCitaId(orden.getAppointment().getId());
        dto.setPacienteId(orden.getPatient().getId());
        dto.setProfesionalId(orden.getProfessional().getId());
        dto.setFechaSolicitud(orden.getFechaSolicitud());
        dto.setEstado(orden.getEstado());
        dto.setObservacionesGenerales(orden.getObservacionesGenerales());
        dto.setDocumentoId(orden.getDocumentoId());
        if (orden.getDetalles() != null) {
            List<OrdenExamenDetalleResponseDTO> detallesDTO = orden.getDetalles().stream()
                    .map(d -> {
                        OrdenExamenDetalleResponseDTO detDTO = new OrdenExamenDetalleResponseDTO();
                        detDTO.setId(d.getId());
                        detDTO.setCupsCodigo(d.getCupsCodigo());
                        detDTO.setNombreExamen(d.getNombreExamen());
                        detDTO.setInstruccionesEspecificas(d.getInstruccionesEspecificas());
                        return detDTO;
                    }).collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }
        return dto;
    }
}