package com.healthflow.service;

import com.healthflow.domain.*;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.MedicalRecordRepository;
import com.healthflow.repo.PatientRepository;
import com.healthflow.repo.ProfessionalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ProfessionalRepository professionalRepository;
    private final ZoneId zoneId;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          MedicalRecordRepository medicalRecordRepository,
                          ProfessionalRepository professionalRepository,
                          @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.professionalRepository = professionalRepository;
        this.zoneId = ZoneId.of(tz);
    }

    /**
     * Obtiene los pacientes de un profesional con su estado (atendido/pendiente) y aplicando filtros.
     *
     * @param professionalId ID del profesional.
     * @param nombre         Filtro por nombre (opcional).
     * @param documento      Filtro por documento (opcional).
     * @param fechaDesde     Filtro por fecha de cita desde (opcional).
     * @param fechaHasta     Filtro por fecha de cita hasta (opcional).
     * @return Un objeto con dos listas: pacientesAtendidos y pacientesPendientes.
     */
    @Transactional(readOnly = true)
    public PacientesConEstado obtenerPacientesConEstado(UUID professionalId,
                                                        String nombre,
                                                        String documento,
                                                        LocalDate fechaDesde,
                                                        LocalDate fechaHasta) {

        List<Patient> todosPacientes = patientRepository.findPatientsByProfessionalId(professionalId);

        OffsetDateTime start = (fechaDesde != null) ? fechaDesde.atStartOfDay(zoneId).toOffsetDateTime() : null;
        OffsetDateTime end = (fechaHasta != null) ? fechaHasta.atTime(LocalTime.MAX).atZone(zoneId).toOffsetDateTime() : null;

        Stream<Patient> stream = todosPacientes.stream();

        if (nombre != null && !nombre.trim().isEmpty()) {
            String nombreLower = nombre.toLowerCase();
            stream = stream.filter(p -> (p.getFirstName() + " " + p.getLastName()).toLowerCase().contains(nombreLower));
        }

        if (documento != null && !documento.trim().isEmpty()) {
            String docLower = documento.toLowerCase();
            stream = stream.filter(p -> p.getDocNumber().toLowerCase().contains(docLower));
        }

        // Filtro de fecha (opcional) - si se implementa, debe hacerse con cuidado, pero por ahora lo omitimos

        List<Patient> pacientesFiltrados = stream.collect(Collectors.toList());

        List<PacienteResumen> atendidos = new ArrayList<>();
        List<PacienteResumen> pendientes = new ArrayList<>();

        for (Patient patient : pacientesFiltrados) {
            // Obtener última cita con este profesional
            Appointment ultimaCita = appointmentRepository
                    .findTopByPatientIdAndProfessionalIdOrderByDateTimeDesc(patient.getId(), professionalId)
                    .orElse(null);

            // Verificar si ha sido atendido por este profesional (tiene algún MedicalRecord)
            boolean tieneRegistro = medicalRecordRepository.existsByAppointmentPatientIdAndAppointmentProfessionalId(patient.getId(), professionalId);

            PacienteResumen resumen = new PacienteResumen(
                    patient.getId(),
                    patient.getFirstName() + " " + patient.getLastName(),
                    patient.getDocNumber(),
                    ultimaCita != null ? ultimaCita.getDateTime() : null,
                    tieneRegistro
            );

            if (tieneRegistro) {
                atendidos.add(resumen);
            } else {
                pendientes.add(resumen);
            }
        }

        return new PacientesConEstado(atendidos, pendientes);
    }

    /**
     * Obtiene el historial completo de un paciente con el profesional actual.
     *
     * @param patientId      ID del paciente.
     * @param professionalId ID del profesional.
     * @return Lista de citas con sus registros médicos asociados.
     */
    @Transactional(readOnly = true)
    public List<CitaConRegistro> obtenerHistorialPaciente(UUID patientId, UUID professionalId) {
        List<Appointment> citas = appointmentRepository.findByPatientIdAndProfessionalIdOrderByDateTimeDesc(
                patientId, professionalId);

        return citas.stream()
                .map(cita -> new CitaConRegistro(
                        cita.getId(),
                        cita.getDateTime(),
                        cita.getStatus(),
                        cita.getMedicalRecord() // puede ser null si no hay registro
                ))
                .collect(Collectors.toList());
    }

    // ========== DTOs internos ==========

    public static class PacienteResumen {
        private final UUID id;
        private final String nombreCompleto;
        private final String documento;
        private final OffsetDateTime ultimaCita;
        private final boolean atendido;

        public PacienteResumen(UUID id, String nombreCompleto, String documento,
                               OffsetDateTime ultimaCita, boolean atendido) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.documento = documento;
            this.ultimaCita = ultimaCita;
            this.atendido = atendido;
        }

        // Getters (necesarios para Thymeleaf)
        public UUID getId() { return id; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getDocumento() { return documento; }
        public OffsetDateTime getUltimaCita() { return ultimaCita; }
        public boolean isAtendido() { return atendido; }
    }

    public static class CitaConRegistro {
        private final UUID id;
        private final OffsetDateTime fecha;
        private final AppointmentStatus estado;
        private final MedicalRecord registro;

        public CitaConRegistro(UUID id, OffsetDateTime fecha, AppointmentStatus estado, MedicalRecord registro) {
            this.id = id;
            this.fecha = fecha;
            this.estado = estado;
            this.registro = registro;
        }

        // Getters
        public UUID getId() { return id; }
        public OffsetDateTime getFecha() { return fecha; }
        public AppointmentStatus getEstado() { return estado; }
        public MedicalRecord getRegistro() { return registro; }
        public boolean tieneRegistro() { return registro != null; }
    }

    public static class PacientesConEstado {
        private final List<PacienteResumen> atendidos;
        private final List<PacienteResumen> pendientes;

        public PacientesConEstado(List<PacienteResumen> atendidos, List<PacienteResumen> pendientes) {
            this.atendidos = atendidos;
            this.pendientes = pendientes;
        }

        public List<PacienteResumen> getAtendidos() { return atendidos; }
        public List<PacienteResumen> getPendientes() { return pendientes; }
    }
}