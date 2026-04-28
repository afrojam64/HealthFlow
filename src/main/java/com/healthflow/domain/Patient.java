package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pacientes")
public class Patient extends BaseEntity {

    @NotBlank
    @Column(name = "tipo_doc", nullable = false, length = 2)
    private String docType;

    @NotBlank
    @Column(name = "num_doc", unique = true, nullable = false, length = 20)
    private String docNumber;

    @NotBlank
    @Column(name = "nombre1", nullable = false, length = 50)
    private String firstName;

    @Column(name = "nombre2", length = 50)
    private String middleName;

    @NotBlank
    @Column(name = "apellido1", nullable = false, length = 50)
    private String lastName;

    @Column(name = "apellido2", length = 50)
    private String secondLastName;

    @NotNull
    @Column(name = "fecha_nac", nullable = false)
    private LocalDate birthDate;

    @NotBlank
    @Column(name = "sexo", nullable = false, length = 1)
    private String sex;

    @NotBlank
    @Column(name = "cod_municipio", nullable = false, length = 5)
    private String municipalityCode;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @NotBlank
    @Column(name = "celular", nullable = false, length = 15)
    private String phone;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PacienteToken> tokens = new ArrayList<>();

    @Column(name = "tipo_usuario", length = 2)
    private String userType = "01";

    @Column(name = "cod_pais_residencia", length = 3)
    private String countryResidenceCode = "170";

    @Column(name = "cod_zona_residencia", length = 2)
    private String zoneResidenceCode = "01";

    // Getters y Setters
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public List<PacienteToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<PacienteToken> tokens) {
        this.tokens = tokens;
    }

    public String getSecondLastName() { return secondLastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getMunicipalityCode() { return municipalityCode; }
    public void setMunicipalityCode(String municipalityCode) { this.municipalityCode = municipalityCode; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getCountryResidenceCode() { return countryResidenceCode; }
    public void setCountryResidenceCode(String countryResidenceCode) { this.countryResidenceCode = countryResidenceCode; }

    public String getZoneResidenceCode() { return zoneResidenceCode; }
    public void setZoneResidenceCode(String zoneResidenceCode) { this.zoneResidenceCode = zoneResidenceCode; }
}
