package com.healthflow.repo;

import com.healthflow.domain.CatalogoCausaExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CatalogoCausaExternaRepository extends JpaRepository<CatalogoCausaExterna, Long> {
    Optional<CatalogoCausaExterna> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}