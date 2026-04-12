package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "profesionales")
public class Professional extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private User user;

    @NotBlank
    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String fullName;

    @Column(name = "nit", unique = true, length = 20)
    private String nit; // Número de Identificación Tributaria (para facturación)

    @NotBlank
    @Column(name = "registro_medico", unique = true, nullable = false, length = 50)
    private String medicalRegistry;

    @NotBlank
    @Column(name = "especialidad", nullable = false, length = 100)
    private String specialty;

    @Column(name = "slug", unique = true, length = 100)
    private String slug;

    @Column(name = "firma_url", length = 500)
    private String firmaUrl;

    // +++++ getter y setter para slug
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    // Getters y Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getMedicalRegistry() {
        return medicalRegistry;
    }

    public void setMedicalRegistry(String medicalRegistry) {
        this.medicalRegistry = medicalRegistry;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getFirmaUrl() { return firmaUrl; }

    public void setFirmaUrl(String firmaUrl) { this.firmaUrl = firmaUrl; }
}
