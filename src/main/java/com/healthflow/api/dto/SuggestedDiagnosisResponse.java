package com.healthflow.api.dto;

public class SuggestedDiagnosisResponse {
    private String diagnosisBase;
    private String subcategory;
    private String stage;
    private String grade;
    private String extent;
    private String stability;
    private String fullText;
    private PeriodontalIndicatorsDTO indicators;

    // Getters y Setters
    public String getDiagnosisBase() { return diagnosisBase; }
    public void setDiagnosisBase(String diagnosisBase) { this.diagnosisBase = diagnosisBase; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getExtent() { return extent; }
    public void setExtent(String extent) { this.extent = extent; }

    public String getStability() { return stability; }
    public void setStability(String stability) { this.stability = stability; }

    public String getFullText() { return fullText; }
    public void setFullText(String fullText) { this.fullText = fullText; }

    public PeriodontalIndicatorsDTO getIndicators() { return indicators; }
    public void setIndicators(PeriodontalIndicatorsDTO indicators) { this.indicators = indicators; }
}
