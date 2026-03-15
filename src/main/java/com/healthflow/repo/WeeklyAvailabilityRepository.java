package com.healthflow.repo;

import com.healthflow.domain.WeeklyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WeeklyAvailabilityRepository extends JpaRepository<WeeklyAvailability, UUID> {

    // Buscar todas las configuraciones de un profesional para una semana específica
    List<WeeklyAvailability> findByProfessionalIdAndWeekStartDateOrderByDayOfWeekAsc(
            UUID professionalId,
            LocalDate weekStartDate
    );

    // Buscar todas las semanas configuradas por un profesional (ordenadas por fecha)
    @Query("SELECT DISTINCT w.weekStartDate FROM WeeklyAvailability w WHERE w.professionalId = :professionalId ORDER BY w.weekStartDate DESC")
    List<LocalDate> findDistinctWeekStartDatesByProfessionalId(@Param("professionalId") UUID professionalId);

    // Eliminar configuraciones de una semana específica
    void deleteByProfessionalIdAndWeekStartDate(UUID professionalId, LocalDate weekStartDate);

    // Verificar si ya existe configuración para una semana
    boolean existsByProfessionalIdAndWeekStartDate(UUID professionalId, LocalDate weekStartDate);
}
