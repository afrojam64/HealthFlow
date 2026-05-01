package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.HashSet;


@Entity
@Table(name = "profesionales")
public class Professional extends BaseEntity {

    // Enumeración para tipo de facturación (puede ir dentro de la clase)
    public enum TipoFacturacion {
        LEGAL,      // Factura electrónica obligatoria
        INFORMAL,   // Sin facturación electrónica (recibo interno)
        SOLO_HC     // Solo historia clínica, no reporta RIPS
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private User user;

    @NotBlank
    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String fullName;

    @Column(name = "nit", unique = true, length = 20)
    private String nit;

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

    @Column(name = "cod_prestador", length = 12)
    private String providerCode;

    // Nuevo campo: tipo de facturación
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_facturacion", nullable = false, length = 10)
    private TipoFacturacion tipoFacturacion = TipoFacturacion.INFORMAL; // valor por defecto

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profesional_especialidad",
            joinColumns = @JoinColumn(name = "professional_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id")
    )
    private Set<Especialidad> especialidades = new HashSet<>();

    // Opcional: método para obtener la especialidad principal (primera de la lista o el campo legacy)
    public String getMainSpecialty() {
        if (especialidades != null && !especialidades.isEmpty()) {
            return especialidades.iterator().next().getNombre();
        }
        return specialty; // fallback al campo antiguo
    }

    // Getters y setters (incluir el nuevo)
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getMedicalRegistry() { return medicalRegistry; }
    public void setMedicalRegistry(String medicalRegistry) { this.medicalRegistry = medicalRegistry; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getFirmaUrl() { return firmaUrl; }
    public void setFirmaUrl(String firmaUrl) { this.firmaUrl = firmaUrl; }

    public TipoFacturacion getTipoFacturacion() { return tipoFacturacion; }
    public void setTipoFacturacion(TipoFacturacion tipoFacturacion) { this.tipoFacturacion = tipoFacturacion; }

    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }

    public Set<Especialidad> getEspecialidades() {
        return especialidades;
    }
    public void setEspecialidades(Set<Especialidad> especialidades) {
        this.especialidades = especialidades;
    }
}