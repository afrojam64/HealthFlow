package com.healthflow.service;

import com.healthflow.api.dto.odontograma.OdontogramaHallazgoDTO;
import com.healthflow.domain.OdontogramaHallazgo;
import com.healthflow.repo.OdontogramaHallazgoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OdontogramaService {

    @Autowired
    private OdontogramaHallazgoRepository repository;

    @Transactional(readOnly = true)
    public List<OdontogramaHallazgoDTO> getHallazgosPorCita(UUID citaId) {
        return repository.findByCitaId(citaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void guardarHallazgos(UUID citaId, List<OdontogramaHallazgoDTO> hallazgos) {
        // Eliminar hallazgos existentes para esta cita (opcional según requerimiento)
        // repository.deleteAll(repository.findByCitaId(citaId));

        for (OdontogramaHallazgoDTO dto : hallazgos) {
            OdontogramaHallazgo entity = toEntity(dto);
            entity.setCitaId(citaId);
            repository.save(entity);
        }
    }

    private OdontogramaHallazgoDTO toDTO(OdontogramaHallazgo entity) {
        OdontogramaHallazgoDTO dto = new OdontogramaHallazgoDTO();
        dto.setId(entity.getId());
        dto.setCitaId(entity.getCitaId());
        dto.setDiente(entity.getDiente());
        dto.setCara(entity.getCara());
        dto.setTipoHallazgo(entity.getTipoHallazgo());
        dto.setCupsId(entity.getCupsId());
        dto.setValorJson(entity.getValorJson());
        dto.setEsInicial(entity.getEsInicial());
        return dto;
    }

    private OdontogramaHallazgo toEntity(OdontogramaHallazgoDTO dto) {
        OdontogramaHallazgo entity = new OdontogramaHallazgo();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setCitaId(dto.getCitaId());
        entity.setDiente(dto.getDiente());
        entity.setCara(dto.getCara());
        entity.setTipoHallazgo(dto.getTipoHallazgo());
        entity.setCupsId(dto.getCupsId());
        entity.setValorJson(dto.getValorJson());
        entity.setEsInicial(dto.getEsInicial());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<OdontogramaHallazgoDTO> getInicialesPorPaciente(UUID pacienteId) {
        List<OdontogramaHallazgo> entityList = repository.findInicialesByPatientId(pacienteId);
        return entityList.stream().map(this::toDTO).collect(Collectors.toList());
    }
}