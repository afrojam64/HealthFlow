package com.healthflow.service;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final CatalogoFinalidadConsultaRepository finalidadRepo;
    private final CatalogoCausaExternaRepository causaExternaRepo;
    private final ZoneId zoneId;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                AppointmentRepository appointmentRepository,
                                CatalogoFinalidadConsultaRepository finalidadRepo,
                                CatalogoCausaExternaRepository causaExternaRepo,
                                @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.finalidadRepo = finalidadRepo;
        this.causaExternaRepo = causaExternaRepo;
        this.zoneId = ZoneId.of(tz);
    }

    @Transactional
    public MedicalRecord getOrCreateForAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada con ID: " + appointmentId));

        if (appointment.getMedicalRecord() != null) {
            return appointment.getMedicalRecord();
        }

        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setAppointment(appointment);
        return medicalRecordRepository.save(newRecord);
    }

    /**
     * Persiste los datos clínicos de una consulta, incluyendo los tres campos separados
     * (enfermedad actual, examen físico, concepto) en lugar del antiguo campo "evolución".
     * También guarda diagnósticos, RIPS y prescripción.
     * No modifica registros bloqueados ni citas de fechas distintas a la actual.
     */
    @Transactional
    public MedicalRecord saveMedicalRecord(UUID appointmentId,
                                           String reason,
                                           String enfermedadActual,
                                           String examenFisico,
                                           String concepto,
                                           String prescription,
                                           String mainDiagnosis,
                                           Long finalidadId,
                                           Long causaExternaId,
                                           BigDecimal valorServicio,
                                           BigDecimal cuotaModeradora,
                                           BigDecimal copago,
                                           String codigoCups,
                                           String relatedDiagnosis1,
                                           String relatedDiagnosis2,
                                           String complicationDiagnosis) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada con ID: " + appointmentId));

        MedicalRecord record = appointment.getMedicalRecord();
        if (record == null) {
            throw new DomainException("No se puede guardar una historia clínica que no ha sido iniciada.");
        }

        if (record.getLocked()) {
            throw new DomainException("Esta consulta ya ha sido cerrada y no se puede modificar.");
        }

        LocalDate today = LocalDate.now(zoneId);
        LocalDate appointmentDate = record.getAppointment().getDateTime().atZoneSameInstant(zoneId).toLocalDate();
        if (!today.isEqual(appointmentDate)) {
            throw new DomainException("La historia clínica solo puede ser editada el día de la cita.");
        }

        // Asignar los campos clínicos
        record.setReason(reason);
        record.setEnfermedadActual(enfermedadActual);
        record.setExamenFisico(examenFisico);
        record.setConcepto(concepto);
        record.setPrescription(prescription);
        record.setMainDiagnosis(mainDiagnosis);
        record.setRelatedDiagnosis1(relatedDiagnosis1);
        record.setRelatedDiagnosis2(relatedDiagnosis2);
        record.setComplicationDiagnosis(complicationDiagnosis);

        // RIPS: finalidad consulta
        if (finalidadId != null) {
            CatalogoFinalidadConsulta finalidad = finalidadRepo.findById(finalidadId)
                    .orElseThrow(() -> new DomainException("Finalidad de consulta no válida"));
            record.setFinalidadConsulta(finalidad);
        } else {
            record.setFinalidadConsulta(null);
        }

        // RIPS: causa externa
        if (causaExternaId != null) {
            CatalogoCausaExterna causa = causaExternaRepo.findById(causaExternaId)
                    .orElseThrow(() -> new DomainException("Causa externa no válida"));
            record.setCausaExterna(causa);
        } else {
            record.setCausaExterna(null);
        }

        // Valores económicos y CUPS
        record.setValorServicio(valorServicio);
        record.setCuotaModeradora(cuotaModeradora);
        record.setCopago(copago);
        record.setCodigoCups(codigoCups);

        return medicalRecordRepository.save(record);
    }


    @Transactional
    public void markAsAttendedAndLock(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada con ID: " + appointmentId));

        MedicalRecord record = appointment.getMedicalRecord();
        if (record == null) {
            throw new DomainException("No se puede marcar como atendida una cita sin una historia clínica iniciada.");
        }

        // Verificar que no esté ya bloqueada
        if (record.getLocked()) {
            throw new DomainException("La historia clínica ya está cerrada.");
        }

        record.setLocked(true);
        medicalRecordRepository.saveAndFlush(record);  // Forzar escritura inmediata

        appointment.setStatus(AppointmentStatus.ATENDIDA);
        appointmentRepository.save(appointment);
    }
}