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
        System.out.println("DEBUG PermisoService - asistenteId recibido: " + asistenteId);
        List<String> perms = permisoRepository.findPermisosByAsistenteId(asistenteId);
        System.out.println("DEBUG PermisoService - resultado: " + perms);
        return perms;
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

    // Agregar estos métodos en PermisoService

    @Transactional
    public void agregarPermiso(UUID medicoId, UUID asistenteId, String permiso) {
        if (!permisoRepository.existsByAsistenteIdAndPermiso(asistenteId, permiso)) {
            AsistentePermiso ap = new AsistentePermiso();
            ap.setMedicoId(medicoId);
            ap.setAsistenteId(asistenteId);
            ap.setPermiso(permiso);
            ap.setConcedido(true);
            permisoRepository.save(ap);
        }
    }

    @Transactional
    public void revocarPermiso(UUID asistenteId, String permiso) {
        permisoRepository.deleteByAsistenteIdAndPermiso(asistenteId, permiso);
    }

    @Transactional
    public void actualizarPermisos(UUID medicoId, UUID asistenteId, List<String> nuevosPermisos) {
        // ✅ Forzar al menos un permiso básico
        if (nuevosPermisos == null || nuevosPermisos.isEmpty()) {
            nuevosPermisos = List.of("VER_CALENDARIO");
        }

        List<String> actuales = getPermisosDeAsistente(asistenteId);

        // Revocar los que ya no están
        for (String p : actuales) {
            if (!nuevosPermisos.contains(p)) {
                revocarPermiso(asistenteId, p);
            }
        }

        // Agregar los nuevos
        for (String p : nuevosPermisos) {
            if (!actuales.contains(p)) {
                agregarPermiso(medicoId, asistenteId, p);
            }
        }
    }
}