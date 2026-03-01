package com.healthflow.service;

import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessionalService {
  private final ProfessionalRepository professionalRepository;

  public ProfessionalService(ProfessionalRepository professionalRepository) {
    this.professionalRepository = professionalRepository;
  }

  @Transactional
  public Professional create(Professional p) {
    return professionalRepository.save(p);
  }

  public Professional get(java.util.UUID id) {
    return professionalRepository.findById(id)
        .orElseThrow(() -> new DomainException("Profesional no encontrado"));
  }
}
