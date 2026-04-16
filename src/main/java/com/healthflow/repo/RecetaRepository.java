package com.healthflow.repo;

import com.healthflow.domain.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecetaRepository extends JpaRepository<Receta, UUID> {
    Optional<Receta> findByToken(UUID token);
    List<Receta> findByPatientIdOrderByFechaEmisionDesc(UUID patientId);
    List<Receta> findByProfessionalIdOrderByFechaEmisionDesc(UUID professionalId);
    Optional<Receta> findByNumero(String numero);
}