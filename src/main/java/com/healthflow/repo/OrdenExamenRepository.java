package com.healthflow.repo;

import com.healthflow.domain.OrdenExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdenExamenRepository extends JpaRepository<OrdenExamen, UUID> {

    // Obtener todas las órdenes de una cita
    List<OrdenExamen> findByAppointmentId(UUID appointmentId);

    // Obtener órdenes de un paciente
    List<OrdenExamen> findByPatientIdOrderByFechaSolicitudDesc(UUID patientId);

    // Obtener órdenes de un profesional
    List<OrdenExamen> findByProfessionalIdOrderByFechaSolicitudDesc(UUID professionalId);

    // Obtener la orden más reciente de un paciente
    Optional<OrdenExamen> findTopByPatientIdOrderByFechaSolicitudDesc(UUID patientId);

    // Obtener órdenes entre dos fechas
    List<OrdenExamen> findByFechaSolicitudBetween(OffsetDateTime start, OffsetDateTime end);

    // Obtener órdenes por estado
    List<OrdenExamen> findByEstado(String estado);

    // Obtener una orden con sus detalles cargados
    @Query("SELECT o FROM OrdenExamen o LEFT JOIN FETCH o.detalles WHERE o.id = :id")
    Optional<OrdenExamen> findByIdWithDetalles(@Param("id") UUID id);
}