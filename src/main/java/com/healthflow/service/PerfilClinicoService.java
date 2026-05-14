package com.healthflow.service;

import com.healthflow.api.dto.CatalogoExamenDTO;
import com.healthflow.api.dto.PerfilClinicoDTO;
import com.healthflow.domain.CatalogoExamen;
import com.healthflow.domain.PerfilClinico;
import com.healthflow.repo.PerfilClinicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PerfilClinicoService {

    private final PerfilClinicoRepository perfilClinicoRepository;

    public PerfilClinicoService(PerfilClinicoRepository perfilClinicoRepository) {
        this.perfilClinicoRepository = perfilClinicoRepository;
    }

    @Transactional(readOnly = true)
    public List<PerfilClinicoDTO> listarTodos() {
        return perfilClinicoRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PerfilClinicoDTO obtenerConExamenes(UUID id) {
        return perfilClinicoRepository.findByIdWithExamenes(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PerfilClinicoDTO> buscarPorTexto(String texto) {
        return perfilClinicoRepository.buscarPorTexto(texto)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private PerfilClinicoDTO toDTO(PerfilClinico perfil) {
        PerfilClinicoDTO dto = new PerfilClinicoDTO();
        dto.setId(perfil.getId());
        dto.setNombre(perfil.getNombre());
        dto.setDescripcion(perfil.getDescripcion());
        if (perfil.getExamenes() != null) {
            List<CatalogoExamenDTO> examenes = perfil.getExamenes().stream()
                    .map(this::toExamenDTO)
                    .collect(Collectors.toList());
            dto.setExamenes(examenes);
        }
        return dto;
    }

    private CatalogoExamenDTO toExamenDTO(CatalogoExamen examen) {
        CatalogoExamenDTO dto = new CatalogoExamenDTO();
        dto.setId(examen.getId());
        dto.setCodigoCups(examen.getCodigoCups());
        dto.setNombre(examen.getNombre());
        dto.setCategoria(examen.getCategoria());
        return dto;
    }
}