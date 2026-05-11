package com.healthflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthflow.domain.Periodontograma;
import com.healthflow.repo.PeriodontogramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PeriodontogramaService {

    private final PeriodontogramaRepository periodontogramaRepository;
    private final ObjectMapper objectMapper;

    private static final double BOP_THRESHOLD_HEALTH = 0.10; // 10%
    private static final int CAL_STAGE_I = 2;
    private static final int CAL_STAGE_II = 3;
    private static final int CAL_STAGE_III = 5;

    public PeriodontogramaService(PeriodontogramaRepository periodontogramaRepository,
                                  ObjectMapper objectMapper) {
        this.periodontogramaRepository = periodontogramaRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Periodontograma save(Periodontograma periodontograma) {
        PeriodontalIndicators indicators = calculateIndicators(periodontograma.getMeasurementsJson());
        SuggestedDiagnosis suggested = suggestDiagnosis(indicators);

        periodontograma.setDiagnosisBase(suggested.getDiagnosisBase());
        periodontograma.setSubcategory(suggested.getSubcategory());
        periodontograma.setStage(suggested.getStage());
        periodontograma.setGrade(suggested.getGrade());
        periodontograma.setExtent(suggested.getExtent());
        periodontograma.setStability(suggested.getStability());
        periodontograma.setFinalDiagnosisText(suggested.getFullText());

        return periodontogramaRepository.save(periodontograma);
    }

    public PeriodontalIndicators calculateIndicators(String measurementsJson) {
        try {
            JsonNode root = objectMapper.readTree(measurementsJson);
            JsonNode dientesNode = root.path("dientes");
            List<JsonNode> allDientes = new ArrayList<>();
            dientesNode.fields().forEachRemaining(entry -> allDientes.add(entry.getValue()));

            int totalSites = 0;
            int bleedingSites = 0;
            int maxCAL = 0;
            int affectedTeethCAL = 0;
            int maxMobility = 0;
            int maxFurcation = 0;
            int totalTeeth = 0;

            for (JsonNode diente : allDientes) {
                totalTeeth++;
                boolean toothAffected = false;
                for (String cara : Arrays.asList("vestibular", "palatino", "lingual")) {
                    JsonNode caraNode = diente.path(cara);
                    if (caraNode.isMissingNode()) continue;

                    for (String pos : Arrays.asList("mesial", "central", "distal")) {
                        int depth = caraNode.path("profundidad_" + pos).asInt(0);
                        int recession = caraNode.path("recesion_" + pos).asInt(0);
                        boolean bleeding = caraNode.path("sangrado_" + pos).asBoolean(false);
                        totalSites++;
                        if (bleeding) bleedingSites++;
                        int cal = depth + recession;
                        if (cal > maxCAL) maxCAL = cal;
                        if (pos.equals("mesial") || pos.equals("distal")) {
                            if (cal >= 2) toothAffected = true;
                        }
                    }
                }
                if (toothAffected) affectedTeethCAL++;
                int mobility = diente.path("movilidad").asInt(0);
                if (mobility > maxMobility) maxMobility = mobility;
                int furcation = diente.path("furca").asInt(0);
                if (furcation > maxFurcation) maxFurcation = furcation;
            }

            double bopPercent = totalSites == 0 ? 0 : (double) bleedingSites / totalSites;
            int lostTeeth = 0;

            return new PeriodontalIndicators(bopPercent, maxCAL, affectedTeethCAL, maxMobility,
                    maxFurcation, lostTeeth, totalTeeth);
        } catch (JsonProcessingException e) {
            throw new DomainException("Error al parsear mediciones JSON: " + e.getMessage());
        }
    }

    public SuggestedDiagnosis suggestDiagnosis(PeriodontalIndicators indicators) {
        if (indicators.getBopPercent() < BOP_THRESHOLD_HEALTH && indicators.getMaxCAL() <= 1 && indicators.getAffectedTeethCAL() < 2) {
            String subcategory = "Periodonto intacto";
            if (indicators.getLostTeeth() > 0 || indicators.getMaxCAL() > 1) {
                subcategory = "Periodonto reducido (no periodontitis)";
            }
            return new SuggestedDiagnosis("Salud Gingival", subcategory, null, null, null, null,
                    "Salud periodontal - " + subcategory.toLowerCase());
        }

        if (indicators.getBopPercent() >= BOP_THRESHOLD_HEALTH && indicators.getMaxCAL() <= 1 && indicators.getAffectedTeethCAL() < 2) {
            String subcategory = indicators.getBopPercent() < 0.30 ? "Asociada solo a biofilm" : "Asociada solo a biofilm (generalizada)";
            return new SuggestedDiagnosis("Gingivitis", subcategory, null, null,
                    indicators.getBopPercent() >= 0.30 ? "GENERALIZADA" : "LOCALIZADA", null,
                    "Gingivitis inducida por biofilm");
        }

        if (indicators.getAffectedTeethCAL() >= 2 && indicators.getMaxCAL() >= 2) {
            String stage = determineStage(indicators);
            String grade = "B";
            String extent = determineExtent(indicators);
            String stability = "INESTABLE";
            String fullText = String.format("Periodontitis Estadio %s Grado %s - %s - %s",
                    stage, grade, extent.equals("GENERALIZADA") ? "Generalizada" : "Localizada", stability);
            return new SuggestedDiagnosis("Periodontitis", null, stage, grade, extent, stability, fullText);
        }

        if (indicators.getMaxMobility() >= 2 || indicators.getMaxFurcation() >= 2) {
            return new SuggestedDiagnosis("Otras Condiciones Periodontales", "Abscesos periodontales agudos",
                    null, null, null, null, "Posible absceso periodontal o trauma oclusal");
        }

        return new SuggestedDiagnosis("Salud Gingival", "Periodonto intacto", null, null, null, null,
                "Sin hallazgos patológicos");
    }

    private String determineStage(PeriodontalIndicators indicators) {
        if (indicators.getMaxCAL() <= CAL_STAGE_I) return "I";
        if (indicators.getMaxCAL() <= CAL_STAGE_II) return "II";
        if (indicators.getMaxCAL() <= CAL_STAGE_III) return "III";
        if (indicators.getLostTeeth() >= 5 || indicators.getMaxMobility() >= 2 || indicators.getMaxFurcation() >= 2) return "IV";
        return "III";
    }

    private String determineExtent(PeriodontalIndicators indicators) {
        double percentAffected = (double) indicators.getAffectedTeethCAL() / indicators.getTotalTeeth();
        return percentAffected >= 0.30 ? "GENERALIZADA" : "LOCALIZADA";
    }

    public Optional<Periodontograma> getLastByPatient(UUID patientId) {
        return periodontogramaRepository.findTopByPatientIdOrderByExamDateDesc(patientId);
    }

    // Clases internas con getters públicos
    public static class PeriodontalIndicators {
        private final double bopPercent;
        private final int maxCAL;
        private final int affectedTeethCAL;
        private final int maxMobility;
        private final int maxFurcation;
        private final int lostTeeth;
        private final int totalTeeth;

        public PeriodontalIndicators(double bopPercent, int maxCAL, int affectedTeethCAL,
                                     int maxMobility, int maxFurcation, int lostTeeth, int totalTeeth) {
            this.bopPercent = bopPercent;
            this.maxCAL = maxCAL;
            this.affectedTeethCAL = affectedTeethCAL;
            this.maxMobility = maxMobility;
            this.maxFurcation = maxFurcation;
            this.lostTeeth = lostTeeth;
            this.totalTeeth = totalTeeth;
        }

        public double getBopPercent() { return bopPercent; }
        public int getMaxCAL() { return maxCAL; }
        public int getAffectedTeethCAL() { return affectedTeethCAL; }
        public int getMaxMobility() { return maxMobility; }
        public int getMaxFurcation() { return maxFurcation; }
        public int getLostTeeth() { return lostTeeth; }
        public int getTotalTeeth() { return totalTeeth; }
    }

    public static class SuggestedDiagnosis {
        private final String diagnosisBase;
        private final String subcategory;
        private final String stage;
        private final String grade;
        private final String extent;
        private final String stability;
        private final String fullText;

        public SuggestedDiagnosis(String diagnosisBase, String subcategory, String stage,
                                  String grade, String extent, String stability, String fullText) {
            this.diagnosisBase = diagnosisBase;
            this.subcategory = subcategory;
            this.stage = stage;
            this.grade = grade;
            this.extent = extent;
            this.stability = stability;
            this.fullText = fullText;
        }

        public String getDiagnosisBase() { return diagnosisBase; }
        public String getSubcategory() { return subcategory; }
        public String getStage() { return stage; }
        public String getGrade() { return grade; }
        public String getExtent() { return extent; }
        public String getStability() { return stability; }
        public String getFullText() { return fullText; }
    }
}