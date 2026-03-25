package com.healthflow.repo;

import com.healthflow.domain.WeeklyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyAvailabilityRepository extends JpaRepository<WeeklyAvailability, UUID> {

    List<WeeklyAvailability> findByProfessionalIdAndWeekStartDateOrderByDayOfWeekAsc(
            UUID professionalId,
            LocalDate weekStartDate
    );

    @Query("SELECT DISTINCT w.weekStartDate FROM WeeklyAvailability w WHERE w.professionalId = :professionalId ORDER BY w.weekStartDate DESC")
    List<LocalDate> findDistinctWeekStartDatesByProfessionalId(@Param("professionalId") UUID professionalId);

    void deleteByProfessionalIdAndWeekStartDate(UUID professionalId, LocalDate weekStartDate);

    boolean existsByProfessionalIdAndWeekStartDate(UUID professionalId, LocalDate weekStartDate);

    // MÉTODO CORREGIDO CON UNA CONSULTA EXPLÍCITA Y ROBUSTA
    @Query("SELECT w FROM WeeklyAvailability w WHERE w.professionalId = :professionalId AND w.weekStartDate = :weekStartDate AND w.dayOfWeek = :dayOfWeek")
    Optional<WeeklyAvailability> findAvailabilityForDay(
        @Param("professionalId") UUID professionalId,
        @Param("weekStartDate") LocalDate weekStartDate,
        @Param("dayOfWeek") int dayOfWeek
    );

    // Nuevo método
    List<WeeklyAvailability> findByProfessionalIdAndWeekStartDateBetween(UUID professionalId, LocalDate start, LocalDate end);
}
