package com.healthflow.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class User {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @NotBlank
  @Column(name = "username", unique = true, nullable = false, length = 50)
  private String username;

  @NotBlank
  @Column(name = "password", nullable = false, length = 255)
  private String passwordHash;

  @Email
  @NotBlank
  @Column(name = "email", unique = true, nullable = false, length = 100)
  private String email;

  @NotBlank
  @Column(name = "rol", nullable = false, length = 20)
  private String role;

  @Column(name = "activo", nullable = false)
  private boolean active = true;

  @PrePersist
  void prePersist() {
    if (id == null) id = UUID.randomUUID();
  }

  public UUID getId() { return id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
