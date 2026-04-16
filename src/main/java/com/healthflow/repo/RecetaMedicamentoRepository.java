package com.healthflow.repo;

import com.healthflow.domain.RecetaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RecetaMedicamentoRepository extends JpaRepository<RecetaMedicamento, UUID> {
    List<RecetaMedicamento> findByRecetaId(UUID recetaId);
}