package com.healthflow.repo;

import com.healthflow.domain.Professional;
import com.healthflow.domain.WeeklyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WeeklyAvailabilityRepository extends JpaRepository<WeeklyAvailability, UUID> {

    List<WeeklyAvailability> findByProfessional_IdAndWeekStartDateOrderByDayOfWeekAsc(
            UUID professionalId,
            LocalDate weekStartDate
    );

    @Query("SELECT DISTINCT w.weekStartDate FROM WeeklyAvailability w WHERE w.professional.id = :professionalId ORDER BY w.weekStartDate DESC")
    List<LocalDate> findDistinctWeekStartDatesByProfessional_Id(@Param("professionalId") UUID professionalId);

    void deleteByProfessional_IdAndWeekStartDate(UUID professionalId, LocalDate weekStartDate);

    boolean existsByProfessional_IdAndWeekStartDate(UUID professionalId, LocalDate weekStartDate);

    // NUEVO: Para el dashboard del doctor
    List<WeeklyAvailability> findByProfessionalAndWeekStartDateGreaterThanEqualOrderByWeekStartDateAsc(
            Professional professional,
            LocalDate startDate
    );
}
