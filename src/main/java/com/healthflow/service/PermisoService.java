package com.healthflow.service;

import com.healthflow.domain.AsistentePermiso;
import com.healthflow.repo.AsistentePermisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PermisoService {

    private final AsistentePermisoRepository permisoRepository;

    public PermisoService(AsistentePermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Transactional(readOnly = true)
    public List<String> getPermisosDeAsistente(UUID asistenteId) {
        return permisoRepository.findPermisosByAsistenteId(asistenteId);
    }

    @Transactional(readOnly = true)
    public boolean tienePermiso(UUID asistenteId, String permiso) {
        return permisoRepository.existsByAsistenteIdAndPermiso(asistenteId, permiso);
    }

    @Transactional(readOnly = true)
    public UUID getMedicoIdByAsistente(UUID asistenteId) {
        List<AsistentePermiso> permisos = permisoRepository.findByAsistenteId(asistenteId);
        if (permisos.isEmpty()) {
            return null;
        }
        // Todos los permisos del mismo asistente tienen el mismo medicoId
        return permisos.get(0).getMedicoId();
    }
}