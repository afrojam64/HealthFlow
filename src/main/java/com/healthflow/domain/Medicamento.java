package com.healthflow.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "medicamentos")
public class Medicamento extends BaseEntity {

    @Column(name = "nombre_generico", nullable = false, length = 200)
    private String nombreGenerico;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    @Column(name = "concentracion", length = 50)
    private String concentracion;

    @Column(name = "forma_farmaceutica", length = 100)
    private String formaFarmaceutica;

    @Column(name = "via_administracion", length = 50)
    private String viaAdministracion;

    // Getters y setters
    public String getNombreGenerico() { return nombreGenerico; }
    public void setNombreGenerico(String nombreGenerico) { this.nombreGenerico = nombreGenerico; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }
    public String getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(String formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }
    public String getViaAdministracion() { return viaAdministracion; }
    public void setViaAdministracion(String viaAdministracion) { this.viaAdministracion = viaAdministracion; }
}