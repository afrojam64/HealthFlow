package com.healthflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthflow.domain.*;
import com.healthflow.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final RecetaMedicamentoRepository recetaMedicamentoRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AppointmentRepository appointmentRepository;
    private final ObjectMapper objectMapper;
    private final ZoneId zoneId;
    private final int expirationDays;


    public RecetaService(RecetaRepository recetaRepository,
                         RecetaMedicamentoRepository recetaMedicamentoRepository,
                         PatientRepository patientRepository,
                         ProfessionalRepository professionalRepository,
                         MedicamentoRepository medicamentoRepository, AppointmentRepository appointmentRepository,
                         ObjectMapper objectMapper,
                         @Value("${healthflow.timezone:America/Bogota}") String timezone,
                         @Value("${healthflow.receta.expiration-days:30}") int expirationDays) {
        this.recetaRepository = recetaRepository;
        this.recetaMedicamentoRepository = recetaMedicamentoRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.appointmentRepository = appointmentRepository;
        this.objectMapper = objectMapper;
        this.zoneId = ZoneId.of(timezone);
        this.expirationDays = expirationDays;
    }

    @Transactional
    public Receta crearRecetaDesdeJson(UUID appointmentId, String prescriptionJson, String observaciones) throws Exception {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada"));
        Patient patient = appointment.getPatient();
        Professional professional = appointment.getProfessional();

        // Parsear JSON de medicamentos
        List<Map<String, String>> medicamentosList = objectMapper.readValue(prescriptionJson,
                new TypeReference<List<Map<String, String>>>() {});

        if (medicamentosList.isEmpty()) {
            throw new DomainException("No hay medicamentos para generar receta");
        }

        // Generar número de receta único (formato: REC-YYYYMMDD-XXXXX)
        String numero = generarNumeroReceta();

        // Crear receta
        Receta receta = new Receta();
        receta.setNumero(numero);
        receta.setPatient(patient);
        receta.setProfessional(professional);
        receta.setAppointment(appointment);
        receta.setFechaEmision(OffsetDateTime.now(zoneId));
        receta.setFechaExpiracion(OffsetDateTime.now(zoneId).plusDays(expirationDays));
        receta.setEstado("ACTIVA");
        receta.setToken(UUID.randomUUID());
        receta.setObservaciones(observaciones);
        receta = recetaRepository.save(receta);

        // Guardar medicamentos
        for (Map<String, String> medMap : medicamentosList) {
            String medicamentoIdStr = medMap.get("id");
            if (medicamentoIdStr == null) continue;
            UUID medicamentoId = UUID.fromString(medicamentoIdStr);
            Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                    .orElseThrow(() -> new DomainException("Medicamento no encontrado: " + medicamentoId));

            RecetaMedicamento rm = new RecetaMedicamento();
            rm.setReceta(receta);
            rm.setMedicamento(medicamento);
            rm.setDosis(medMap.getOrDefault("dosis", ""));
            rm.setFrecuencia(medMap.getOrDefault("frecuencia", ""));
            rm.setCantidad(Integer.parseInt(medMap.getOrDefault("cantidad", "1")));
            rm.setInstrucciones(medMap.getOrDefault("instrucciones", null));
            recetaMedicamentoRepository.save(rm);
        }

        return receta;
    }

    private synchronized String generarNumeroReceta() {
        // Obtener el último número generado (simplificado: contar registros del día)
        String prefix = OffsetDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = recetaRepository.count(); // Podría hacerse más específico por día
        return String.format("REC-%s-%05d", prefix, count + 1);
    }

    public Receta getRecetaByToken(UUID token) {
        return recetaRepository.findByToken(token)
                .orElseThrow(() -> new DomainException("Receta no encontrada"));
    }

    @Transactional
    public void anularReceta(UUID recetaId, UUID professionalId) {
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new DomainException("Receta no encontrada"));
        if (!receta.getProfessional().getId().equals(professionalId)) {
            throw new DomainException("No tienes permiso para anular esta receta");
        }
        if ("ANULADA".equals(receta.getEstado()) || "DISPENSADA".equals(receta.getEstado())) {
            throw new DomainException("La receta no se puede anular porque ya está " + receta.getEstado());
        }
        receta.setEstado("ANULADA");
        recetaRepository.save(receta);
    }
}