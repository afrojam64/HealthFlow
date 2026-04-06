package com.healthflow.repo;

import com.healthflow.domain.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface MedicamentoRepository extends JpaRepository<Medicamento, UUID> {

    @Query("SELECT m FROM Medicamento m WHERE LOWER(m.nombreGenerico) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.nombreGenerico ASC")
    List<Medicamento> searchByNombreGenerico(@Param("query") String query, org.springframework.data.domain.Pageable pageable);
}