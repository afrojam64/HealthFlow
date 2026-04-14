package com.healthflow.domain;

public enum AppointmentStatus {
  PENDIENTE("Pendiente"),
  CONFIRMADA("Confirmada"),
  CANCELADA("Cancelada"),
  ATENDIDA("Atendida"),
  NO_ATENDIDA("No Atendida");

  private final String displayName;

  AppointmentStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
