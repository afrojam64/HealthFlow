package com.healthflow.repo;

import com.healthflow.domain.PerfilClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilClinicoRepository extends JpaRepository<PerfilClinico, UUID> {

    // Buscar por nombre (coincidencia exacta o parcial)
    Optional<PerfilClinico> findByNombreIgnoreCase(String nombre);
    List<PerfilClinico> findByNombreContainingIgnoreCase(String nombre);

    // Perfiles activos
    List<PerfilClinico> findByActivoTrueOrderByNombreAsc();

    // Obtener un perfil con sus exámenes cargados (evita LazyInitializationException)
    @Query("SELECT p FROM PerfilClinico p LEFT JOIN FETCH p.examenes WHERE p.id = :id AND p.activo = true")
    Optional<PerfilClinico> findByIdWithExamenes(@Param("id") UUID id);

    // Búsqueda por texto en nombre o descripción
    @Query("SELECT p FROM PerfilClinico p WHERE p.activo = true AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<PerfilClinico> buscarPorTexto(@Param("texto") String texto);
}