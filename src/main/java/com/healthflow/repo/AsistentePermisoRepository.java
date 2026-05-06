package com.healthflow.repo;

import com.healthflow.domain.AsistentePermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AsistentePermisoRepository extends JpaRepository<AsistentePermiso, UUID> {

    List<AsistentePermiso> findByAsistenteId(UUID asistenteId);

    @Query("SELECT ap.permiso FROM AsistentePermiso ap WHERE ap.asistenteId = :asistenteId AND ap.concedido = true")
    List<String> findPermisosByAsistenteId(@Param("asistenteId") UUID asistenteId);

    List<AsistentePermiso> findByMedicoIdAndAsistenteId(UUID medicoId, UUID asistenteId);

    boolean existsByAsistenteIdAndPermiso(UUID asistenteId, String permiso);

    void deleteByAsistenteIdAndMedicoId(UUID asistenteId, UUID medicoId);

    // En AsistentePermisoRepository
    void deleteByAsistenteIdAndPermiso(UUID asistenteId, String permiso);
    List<AsistentePermiso> findByMedicoId(UUID medicoId);
}