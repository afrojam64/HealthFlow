package com.healthflow.repo;

import com.healthflow.domain.DiagnosticoCIE10;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiagnosticoCIE10Repository extends JpaRepository<DiagnosticoCIE10, Long> {

    Optional<DiagnosticoCIE10> findByCodigo(String codigo);

    @Query("SELECT d FROM DiagnosticoCIE10 d WHERE LOWER(d.codigo) LIKE LOWER(CONCAT('%', :term," +
            "'%')) OR LOWER(d.descripcion) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<DiagnosticoCIE10> searchByTerm(@Param("term") String term);

    /**
     * Busca diagnósticos CIE-10 cuyo código o descripción contenga el texto de búsqueda,
     * ignorando mayúsculas/minúsculas. Retorna máximo 10 resultados.
     */
    @Query("SELECT d FROM DiagnosticoCIE10 d WHERE LOWER(d.codigo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.descripcion) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY d.codigo ASC")
    List<DiagnosticoCIE10> searchByCodeOrDescription(@Param("query") String query, org.springframework.data.domain.Pageable pageable);

    boolean existsByCodigo(String codigo);
}