package com.healthflow.repo;

import com.healthflow.domain.PeriodontalDiagnosisHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PeriodontalDiagnosisRepository extends JpaRepository<PeriodontalDiagnosisHierarchy, Integer> {

    // Nivel 1 activos ordenados por 'order'
    List<PeriodontalDiagnosisHierarchy> findByLevelAndActiveTrueOrderByOrderAsc(Integer level);

    // Subcategorías de un parentId activas ordenadas por 'order'
    List<PeriodontalDiagnosisHierarchy> findByParentIdAndActiveTrueOrderByOrderAsc(Integer parentId);

    // Diagnóstico específico por campos atómicos (para periodontitis)
    Optional<PeriodontalDiagnosisHierarchy> findByStageAndGradeAndExtentAndStabilityAndActiveTrue(
            String stage, String grade, String extent, String stability);

    // Variantes de nivel 3 con stage no nulo
    List<PeriodontalDiagnosisHierarchy> findByLevelAndStageIsNotNullAndActiveTrueOrderByOrderAsc(Integer level);

    // Variantes de nivel 3 con grade no nulo
    List<PeriodontalDiagnosisHierarchy> findByLevelAndGradeIsNotNullAndActiveTrueOrderByOrderAsc(Integer level);

    // Variantes de nivel 3 con extent no nulo
    List<PeriodontalDiagnosisHierarchy> findByLevelAndExtentIsNotNullAndActiveTrueOrderByOrderAsc(Integer level);

    // Variantes de nivel 3 con stability no nulo
    List<PeriodontalDiagnosisHierarchy> findByLevelAndStabilityIsNotNullAndActiveTrueOrderByOrderAsc(Integer level);

    // Diagnóstico por grupo y subcategoría
    Optional<PeriodontalDiagnosisHierarchy> findByGroupAndSubcategoryAndActiveTrue(String group, String subcategory);

    // Diagnósticos de un grupo y nivel 2 activos ordenados
    List<PeriodontalDiagnosisHierarchy> findByGroupAndLevelAndActiveTrueOrderByOrderAsc(String group, Integer level);

    // Búsqueda por descripción parcial
    List<PeriodontalDiagnosisHierarchy> findByDescriptionContainingIgnoreCaseAndActiveTrue(String keyword);
}