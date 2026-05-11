package com.healthflow.api.dto;

public class PeriodontalIndicatorsDTO {
    private double bopPercent;
    private int maxCAL;
    private int affectedTeethCAL;
    private int maxMobility;
    private int maxFurcation;
    private int lostTeeth;
    private int totalTeeth;

    // Getters y Setters
    public double getBopPercent() { return bopPercent; }
    public void setBopPercent(double bopPercent) { this.bopPercent = bopPercent; }

    public int getMaxCAL() { return maxCAL; }
    public void setMaxCAL(int maxCAL) { this.maxCAL = maxCAL; }

    public int getAffectedTeethCAL() { return affectedTeethCAL; }
    public void setAffectedTeethCAL(int affectedTeethCAL) { this.affectedTeethCAL = affectedTeethCAL; }

    public int getMaxMobility() { return maxMobility; }
    public void setMaxMobility(int maxMobility) { this.maxMobility = maxMobility; }

    public int getMaxFurcation() { return maxFurcation; }
    public void setMaxFurcation(int maxFurcation) { this.maxFurcation = maxFurcation; }

    public int getLostTeeth() { return lostTeeth; }
    public void setLostTeeth(int lostTeeth) { this.lostTeeth = lostTeeth; }

    public int getTotalTeeth() { return totalTeeth; }
    public void setTotalTeeth(int totalTeeth) { this.totalTeeth = totalTeeth; }
}