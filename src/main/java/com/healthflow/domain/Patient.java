package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pacientes")
public class Patient {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

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
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @NotBlank
    @Column(name = "celular", nullable = false, length = 15)
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public UUID getId() { return id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}