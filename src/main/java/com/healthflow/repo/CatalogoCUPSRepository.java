package com.healthflow.repo;

import com.healthflow.domain.CatalogoCUPS;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CatalogoCUPSRepository extends JpaRepository<CatalogoCUPS, Long> {
    Optional<CatalogoCUPS> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}