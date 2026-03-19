package com.healthflow.service;

import com.healthflow.api.dto.rips.RipsConsultaDTO;
import com.healthflow.api.dto.rips.RipsDTO;
import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.MedicalRecord;
import com.healthflow.repo.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RipsService {

    private final AppointmentRepository appointmentRepository;
    private final ZoneId zoneId;

    public RipsService(AppointmentRepository appointmentRepository,
                       @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.appointmentRepository = appointmentRepository;
        this.zoneId = ZoneId.of(tz);
    }

    /**
     * Genera el reporte RIPS para un profesional en un rango de fechas.
     *
     * @param professionalId ID del profesional.
     * @param fechaInicio    Fecha de inicio (inclusive).
     * @param fechaFin       Fecha de fin (inclusive).
     * @return DTO con la estructura del reporte RIPS.
     */
    @Transactional(readOnly = true)
    public RipsDTO generarRips(UUID professionalId, LocalDate fechaInicio, LocalDate fechaFin) {

        // Convertir fechas a OffsetDateTime para la consulta (incluye todo el día)
        OffsetDateTime start = fechaInicio.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime end = fechaFin.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();

        // Buscar todas las citas ATENDIDAS en el rango
        List<Appointment> citasAtendidas = appointmentRepository
                .findByProfessionalIdAndDateTimeBetweenAndStatus(
                        professionalId, start, end, AppointmentStatus.ATENDIDA
                );

        // Transformar cada cita en un DTO de consulta RIPS
        List<RipsConsultaDTO> consultas = citasAtendidas.stream()
                .map(this::convertirAConsultaDTO)
                .collect(Collectors.toList());

        // Construir el DTO principal
        RipsDTO rips = new RipsDTO();
        rips.setFechaGeneracion(LocalDate.now(zoneId));
        // Estos datos deberían venir del profesional, pero por ahora los dejamos vacíos
        rips.setNitProfesional(""); // Se obtendría de Professional
        rips.setNombreProfesional(""); // Se obtendría de Professional
        rips.setRegistroRethus(""); // Se obtendría de Professional
        rips.setConsultas(consultas);

        return rips;
    }

    /**
     * Convierte una cita atendida en un DTO de consulta RIPS.
     */
    private RipsConsultaDTO convertirAConsultaDTO(Appointment cita) {
        RipsConsultaDTO dto = new RipsConsultaDTO();

        // Fecha y hora de la atención (en zona local)
        ZonedDateTime fechaHoraLocal = cita.getDateTime().atZoneSameInstant(zoneId);
        dto.setFechaInicioAtencion(fechaHoraLocal.toLocalDate());
        dto.setHoraInicioAtencion(fechaHoraLocal.toLocalTime());

        // Datos del paciente
        dto.setTipoDocumentoIdentificacion(cita.getPatient().getDocType());
        dto.setNumDocumentoIdentificacion(cita.getPatient().getDocNumber());

        // Datos clínicos (si existen)
        MedicalRecord mr = cita.getMedicalRecord();
        if (mr != null) {
            dto.setCodDiagnosticoPrincipal(mr.getMainDiagnosis());
            // Nota: si tienes campos separados para diagnósticos relacionados, asígnarlos aquí
            // dto.setCodDiagnosticoRelacionado1(mr.getRelatedDiagnosis1());
            // dto.setCodDiagnosticoRelacionado2(mr.getRelatedDiagnosis2());

            // Finalidad y causa externa (los códigos)
            if (mr.getFinalidadConsulta() != null) {
                dto.setFinalidadConsulta(mr.getFinalidadConsulta().getCodigo());
            }
            if (mr.getCausaExterna() != null) {
                dto.setCausaExterna(mr.getCausaExterna().getCodigo());
            }

            // Valores económicos (si se capturan)
            dto.setVrServicio(mr.getValorServicio() != null ? mr.getValorServicio().doubleValue() : null);
            dto.setVrCuotaModeradora(mr.getCuotaModeradora() != null ? mr.getCuotaModeradora().doubleValue() : null);
            dto.setVrCopago(mr.getCopago() != null ? mr.getCopago().doubleValue() : null);
        }

        // Código CUPS de la consulta (podrías tenerlo en el profesional o en un catálogo)
        // Por ahora lo dejamos vacío o con un valor por defecto.
        dto.setCodigoConsulta(""); // Ej: "890101" para consulta de medicina general

        return dto;
    }
}