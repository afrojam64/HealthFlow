package com.healthflow.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "periodontal_diagnosis_hierarchy")
public class PeriodontalDiagnosisHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "nivel", nullable = false)
    private Integer level;

    @Column(name = "grupo", length = 50)
    private String group;

    @Column(name = "subcategoria", length = 100)
    private String subcategory;

    @Column(name = "stage", length = 2)
    private String stage;

    @Column(name = "grade", length = 1)
    private String grade;

    @Column(name = "extension", length = 20)
    private String extent;

    @Column(name = "estabilidad", length = 20)
    private String stability;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cie10_code", length = 10)
    private String cie10Code;

    @Column(name = "orden")
    private Integer order;

    @Column(name = "activo")
    private Boolean active = true;

    // Getters y setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    public String getStability() {
        return stability;
    }

    public void setStability(String stability) {
        this.stability = stability;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCie10Code() {
        return cie10Code;
    }

    public void setCie10Code(String cie10Code) {
        this.cie10Code = cie10Code;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}