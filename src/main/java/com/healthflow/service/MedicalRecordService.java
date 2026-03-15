package com.healthflow.service;

import com.healthflow.domain.Appointment;
import com.healthflow.domain.AppointmentStatus;
import com.healthflow.domain.MedicalRecord;
import com.healthflow.repo.AppointmentRepository;
import com.healthflow.repo.MedicalRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository, AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public MedicalRecord saveMedicalRecord(UUID appointmentId, String reason, String evolution, String prescription, String mainDiagnosis) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Cita no encontrada con ID: " + appointmentId));

        if (appointment.getMedicalRecord() != null && appointment.getMedicalRecord().getLocked()) {
            throw new DomainException("Esta consulta ya ha sido cerrada y no se puede modificar.");
        }

        MedicalRecord record = appointment.getMedicalRecord();
        if (record == null) {
            record = new MedicalRecord();
            record.setAppointment(appointment);
        }

        record.setReason(reason);
        record.setEvolution(evolution);
        record.setPrescription(prescription);
        record.setMainDiagnosis(mainDiagnosis);
        record.setLocked(true);

        MedicalRecord savedRecord = medicalRecordRepository.save(record);

        appointment.setStatus(AppointmentStatus.ATENDIDA);
        appointmentRepository.save(appointment);

        return savedRecord;
    }
}
