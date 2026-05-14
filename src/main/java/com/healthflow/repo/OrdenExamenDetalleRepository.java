package com.healthflow.repo;

import com.healthflow.domain.OrdenExamenDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface OrdenExamenDetalleRepository extends JpaRepository<OrdenExamenDetalle, UUID> {

    // Obtener detalles de una orden específica
    List<OrdenExamenDetalle> findByOrdenExamenId(UUID ordenId);

    // Eliminar todos los detalles de una orden (útil para reemplazar)
    @Modifying
    @Transactional
    void deleteByOrdenExamenId(UUID ordenId);

    // Contar detalles por orden
    long countByOrdenExamenId(UUID ordenId);

    // Buscar detalles por examen (para saber en qué órdenes se ha solicitado)
    List<OrdenExamenDetalle> findByExamenId(UUID examenId);
}