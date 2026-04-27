package com.healthflow.repo;

import com.healthflow.domain.CatalogoCUPS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CatalogoCUPSRepository extends JpaRepository<CatalogoCUPS, Long> {
    Optional<CatalogoCUPS> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    @Query("SELECT c FROM CatalogoCUPS c WHERE LOWER(c.codigo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY c.codigo ASC")
    List<CatalogoCUPS> searchByCodeOrDescription(@Param("query") String query, Pageable pageable);
}