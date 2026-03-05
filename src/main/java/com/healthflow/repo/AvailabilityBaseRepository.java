package com.healthflow.repo;

import com.healthflow.domain.AvailabilityBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AvailabilityBaseRepository extends JpaRepository<AvailabilityBase, UUID> {
  List<AvailabilityBase> findByProfessionalIdAndDayOfWeek(UUID professionalId, int dayOfWeek);

    List<AvailabilityBase> findByProfessionalIdOrderByDayOfWeekAsc(UUID professionalId);
}
