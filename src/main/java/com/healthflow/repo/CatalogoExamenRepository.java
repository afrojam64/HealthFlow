package com.healthflow.repo;

import com.healthflow.domain.CatalogoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogoExamenRepository extends JpaRepository<CatalogoExamen, UUID> {

    // Búsqueda por código CUPS exacto
    Optional<CatalogoExamen> findByCodigoCups(String codigoCups);

    // Búsqueda por nombre (coincidencia parcial, ignorando mayúsculas/minúsculas)
    List<CatalogoExamen> findByNombreContainingIgnoreCase(String nombre);

    // Búsqueda por categoría y activo
    List<CatalogoExamen> findByCategoriaAndActivoTrue(String categoria);

    // Búsqueda general por texto (nombre o código CUPS)
    @Query("SELECT e FROM CatalogoExamen e WHERE e.activo = true AND (LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(e.codigoCups) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<CatalogoExamen> buscarPorTexto(@Param("texto") String texto);

    // Obtener todos activos ordenados por nombre
    List<CatalogoExamen> findByActivoTrueOrderByNombreAsc();

    // Contar por categoría (útil para estadísticas)
    long countByCategoria(String categoria);
}