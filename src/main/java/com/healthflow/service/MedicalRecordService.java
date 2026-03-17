package com.healthflow.service;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.MedicalRecord;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.MedicalRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final ZoneId zoneId;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                AppointmentRepository appointmentRepository,
                                @org.springframework.beans.factory.annotation.Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.zoneId = ZoneId.of(tz);
    }

    @Transactional
    public MedicalRecord getOrCreateForAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada con ID: " + appointmentId));

        // Si ya existe un registro, lo retornamos
        if (appointment.getMedicalRecord() != null) {
            return appointment.getMedicalRecord();
        }

        // Si no existe, creamos uno nuevo en estado de borrador
        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setAppointment(appointment);
        return medicalRecordRepository.save(newRecord);
    }

    @Transactional
    public MedicalRecord saveMedicalRecord(UUID appointmentId, String reason, String evolution, String prescription, String mainDiagnosis) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada con ID: " + appointmentId));

        MedicalRecord record = appointment.getMedicalRecord();
        if (record == null) {
            throw new DomainException("No se puede guardar una historia clínica que no ha sido iniciada.");
        }

        // Validar que el registro no esté bloqueado
        if (record.getLocked()) {
            throw new DomainException("Esta consulta ya ha sido cerrada y no se puede modificar.");
        }

        // Validar que solo se pueda editar el día de la cita
        LocalDate today = LocalDate.now(zoneId);
        LocalDate appointmentDate = record.getAppointment().getDateTime().atZoneSameInstant(zoneId).toLocalDate();
        if (!today.isEqual(appointmentDate)) {
            throw new DomainException("La historia clínica solo puede ser editada el día de la cita.");
        }

        record.setReason(reason);
        record.setEvolution(evolution);
        record.setPrescription(prescription);
        record.setMainDiagnosis(mainDiagnosis);

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

        record.setLocked(true);
        medicalRecordRepository.save(record);

        appointment.setStatus(AppointmentStatus.ATENDIDA);
        appointmentRepository.save(appointment);
    }
}