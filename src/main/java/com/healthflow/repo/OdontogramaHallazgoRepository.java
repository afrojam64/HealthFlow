package com.healthflow.repo;

import com.healthflow.domain.OdontogramaHallazgo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OdontogramaHallazgoRepository extends JpaRepository<OdontogramaHallazgo, UUID> {

    List<OdontogramaHallazgo> findByCitaId(UUID citaId);

    @Query("SELECT o FROM OdontogramaHallazgo o WHERE o.citaId IN (SELECT a.id FROM Appointment a WHERE a.patient.id = :patientId) AND o.esInicial = true")
    List<OdontogramaHallazgo> findInicialesByPatientId(@Param("patientId") UUID patientId);
}