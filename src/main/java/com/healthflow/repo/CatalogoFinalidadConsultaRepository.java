package com.healthflow.repo;

import com.healthflow.domain.CatalogoFinalidadConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CatalogoFinalidadConsultaRepository extends JpaRepository<CatalogoFinalidadConsulta, Long> {
    Optional<CatalogoFinalidadConsulta> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}