package com.healthflow.service;

import com.healthflow.api.dto.CatalogoExamenDTO;
import com.healthflow.domain.CatalogoExamen;
import com.healthflow.repo.CatalogoExamenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoExamenService {

    private final CatalogoExamenRepository catalogoExamenRepository;

    public CatalogoExamenService(CatalogoExamenRepository catalogoExamenRepository) {
        this.catalogoExamenRepository = catalogoExamenRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogoExamenDTO> buscarPorTexto(String texto) {
        return catalogoExamenRepository.buscarPorTexto(texto)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoExamenDTO> listarPorCategoria(String categoria) {
        return catalogoExamenRepository.findByCategoriaAndActivoTrue(categoria)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CatalogoExamenDTO> listarTodosActivos() {
        return catalogoExamenRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CatalogoExamenDTO toDTO(CatalogoExamen examen) {
        CatalogoExamenDTO dto = new CatalogoExamenDTO();
        dto.setId(examen.getId());
        dto.setCodigoCups(examen.getCodigoCups());
        dto.setNombre(examen.getNombre());
        dto.setDescripcion(examen.getDescripcion());
        dto.setCategoria(examen.getCategoria());
        dto.setModalidad(examen.getModalidad());
        dto.setRegionAnatomica(examen.getRegionAnatomica());
        dto.setRequiereContraste(examen.getRequiereContraste());
        dto.setTipoMuestra(examen.getTipoMuestra());
        dto.setActivo(examen.getActivo());
        return dto;
    }
}