package com.healthflow.service;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RipsService {

    private static final Logger log = LoggerFactory.getLogger(RipsService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private RipsGenerationRepository ripsGenerationRepository;

    @Value("${healthflow.timezone:America/Bogota}")
    private String zoneIdStr;

    @Value("${rips.storage.path:./rips}")
    private String storagePath;

    private final ObjectMapper objectMapper;
    private ZoneId zoneId;

    public RipsService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Autowired
    public void initZoneId() {
        this.zoneId = ZoneId.of(zoneIdStr);
    }

    @Transactional
    public RipsGeneration generarRips(UUID professionalId,
                                      LocalDate fechaDesde,
                                      LocalDate fechaHasta,
                                      String numFacturaGlobal,
                                      boolean automatica) {

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        if (professional.getTipoFacturacion() == Professional.TipoFacturacion.SOLO_HC) {
            throw new RuntimeException("Perfil SOLO_HC no puede generar RIPS.");
        }

        OffsetDateTime start = fechaDesde.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime end = fechaHasta.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        List<Appointment> citas = appointmentRepository.findByProfessionalIdAndDateTimeBetweenAndStatus(
                professionalId, start, end, AppointmentStatus.ATENDIDA);

        if (citas.isEmpty()) {
            throw new RuntimeException("No hay citas atendidas en el período.");
        }

        String numFactura = determinarNumFactura(professional, numFacturaGlobal, citas);
        Map<String, Object> ripsJson = construirRipsJson(professional, citas, numFactura);
        String fileName = generarNombreArchivo(professional, fechaDesde, fechaHasta);
        Path filePath = Paths.get(fileName);

        try {
            Files.createDirectories(filePath.getParent());
            objectMapper.writeValue(filePath.toFile(), ripsJson);
        } catch (IOException e) {
            log.error("Error guardando RIPS", e);
            throw new RuntimeException("No se pudo guardar el archivo RIPS: " + e.getMessage());
        }

        RipsGeneration gen = new RipsGeneration();
        gen.setProfessionalId(professionalId);
        gen.setFechaDesde(fechaDesde);
        gen.setFechaHasta(fechaHasta);
        gen.setFechaGeneracion(LocalDateTime.now());
        gen.setTipoGeneracion(professional.getTipoFacturacion().name());
        gen.setNumFactura(numFactura);
        gen.setArchivoPath(filePath.toString());
        gen.setAutomatica(automatica);
        gen.setTotalRegistros(citas.size());

        return ripsGenerationRepository.save(gen);
    }

    private String determinarNumFactura(Professional professional, String numFacturaGlobal, List<Appointment> citas) {
        if (professional.getTipoFacturacion() == Professional.TipoFacturacion.LEGAL) {
            if (numFacturaGlobal != null && !numFacturaGlobal.isBlank()) {
                if (!numFacturaGlobal.matches("^(FAC|FEV|FVE)-\\d{1,8}$")) {
                    throw new RuntimeException("Formato de factura inválido. Use prefijo FAC-, FEV- o FVE- seguido de números.");
                }
                return numFacturaGlobal;
            } else {
                Set<String> facturasIndividuales = citas.stream()
                        .map(Appointment::getFacturaNumero)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (facturasIndividuales.size() == 1) {
                    return facturasIndividuales.iterator().next();
                } else if (facturasIndividuales.isEmpty()) {
                    throw new RuntimeException("No se proporcionó número de factura global ni hay facturas individuales.");
                } else {
                    throw new RuntimeException("Las citas tienen diferentes números de factura. Proporcione uno global.");
                }
            }
        } else {
            if (numFacturaGlobal != null && !numFacturaGlobal.isBlank()) {
                return numFacturaGlobal;
            } else {
                Integer max = ripsGenerationRepository.findMaxConsecutivoReciboInterno(professional.getId());
                int consecutivo = (max == null) ? 1 : max + 1;
                return "REC-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", consecutivo);
            }
        }
    }

    private String generarNombreArchivo(Professional professional, LocalDate desde, LocalDate hasta) {
        String slug = professional.getSlug() != null ? professional.getSlug() : professional.getId().toString();
        return storagePath + "/rips_" + slug + "_" + desde + "_" + hasta + ".json";
    }

    private Map<String, Object> construirRipsJson(Professional professional, List<Appointment> citas, String numFactura) {
        Map<Patient, List<Appointment>> citasPorPaciente = citas.stream()
                .collect(Collectors.groupingBy(Appointment::getPatient));

        List<Map<String, Object>> usuarios = new ArrayList<>();

        for (Map.Entry<Patient, List<Appointment>> entry : citasPorPaciente.entrySet()) {
            Patient paciente = entry.getKey();
            List<Appointment> citasPaciente = entry.getValue();

            Map<String, Object> usuario = new LinkedHashMap<>();
            usuario.put("tipoDocumentoIdentificacion", paciente.getDocType());
            usuario.put("numDocumentoIdentificacion", paciente.getDocNumber());
            usuario.put("tipoUsuario", Optional.ofNullable(paciente.getUserType()).orElse("01"));
            usuario.put("fechaNacimiento", paciente.getBirthDate().toString());
            usuario.put("codSexo", paciente.getSex());
            usuario.put("codPaisResidencia", Optional.ofNullable(paciente.getCountryResidenceCode()).orElse("170"));
            usuario.put("codMunicipioResidencia", paciente.getMunicipalityCode());
            usuario.put("codZonaResidencia", Optional.ofNullable(paciente.getZoneResidenceCode()).orElse("01"));

            List<Map<String, Object>> consultas = new ArrayList<>();
            for (Appointment cita : citasPaciente) {
                MedicalRecord mr = cita.getMedicalRecord();
                if (mr == null) continue;

                Map<String, Object> consulta = new LinkedHashMap<>();
                consulta.put("codPrestador", Optional.ofNullable(professional.getProviderCode()).orElse("000000000000"));
                consulta.put("fechaInicioAtencion", cita.getDateTime().toLocalDate().toString());
                consulta.put("horaInicioAtencion", cita.getDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                consulta.put("numAutorizacion", null);
                consulta.put("codigoConsulta", Optional.ofNullable(mr.getCodigoCups()).orElse("890101"));
                consulta.put("modalidadGrupoServicio", Optional.ofNullable(mr.getModalidadConsulta()).orElse("01"));
                consulta.put("grupoServicios", Optional.ofNullable(mr.getGrupoServicios()).orElse("01"));
                consulta.put("codServicio", Optional.ofNullable(mr.getCodigoCups()).orElse("394"));
                consulta.put("finalidadConsulta", obtenerCodigoCatalogo(mr.getFinalidadConsulta()));
                consulta.put("causaExterna", obtenerCodigoCatalogo(mr.getCausaExterna()));
                consulta.put("codDiagnosticoPrincipal", mr.getMainDiagnosis());
                consulta.put("codDiagnosticoRelacionado1", mr.getRelatedDiagnosis1());
                consulta.put("codDiagnosticoRelacionado2", mr.getRelatedDiagnosis2());
                consulta.put("codDiagnosticoRelacionado3", mr.getComplicationDiagnosis());
                consulta.put("tipoDiagnosticoPrincipal", Optional.ofNullable(mr.getTipoDiagnostico()).orElse("01"));
                consulta.put("tipoDocumentoIdentificacion", paciente.getDocType());
                consulta.put("numDocumentoIdentificacion", paciente.getDocNumber());
                consulta.put("vrServicio", mr.getValorServicio() != null ? mr.getValorServicio() : BigDecimal.ZERO);
                consulta.put("tipoPagoModerador", "01");
                consulta.put("vrCuotaModeradora", mr.getCuotaModeradora() != null ? mr.getCuotaModeradora() : BigDecimal.ZERO);
                consulta.put("fevNumero", numFactura);

                consultas.add(consulta);
            }

            if (!consultas.isEmpty()) {
                Map<String, Object> servicios = new LinkedHashMap<>();
                servicios.put("consultas", consultas);
                servicios.put("procedimientos", Collections.emptyList());
                servicios.put("medicamentos", Collections.emptyList());
                servicios.put("otrosServicios", Collections.emptyList());
                usuario.put("servicios", servicios);
                usuarios.add(usuario);
            }
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("numDocumentoIdObligado", Optional.ofNullable(professional.getNit()).orElse(""));
        root.put("numFactura", numFactura);
        root.put("tipoNota", null);
        root.put("numNota", null);
        root.put("usuarios", usuarios);

        return root;
    }

    private String obtenerCodigoCatalogo(Object catalogo) {
        if (catalogo == null) return "";
        try {
            if (catalogo instanceof CatalogoFinalidadConsulta) {
                return ((CatalogoFinalidadConsulta) catalogo).getCodigo();
            }
            if (catalogo instanceof CatalogoCausaExterna) {
                return ((CatalogoCausaExterna) catalogo).getCodigo();
            }
            return "";
        } catch (Exception e) {
            log.warn("No se pudo obtener código del catálogo: {}", catalogo.getClass().getSimpleName());
            return "";
        }
    }
}