package com.healthflow.service;

import com.healthflow.repo.CatalogoCUPSRepository;
import com.healthflow.repo.DiagnosticoCIE10Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodigoValidator {

    @Autowired
    private DiagnosticoCIE10Repository cie10Repository;

    @Autowired
    private CatalogoCUPSRepository cupsRepository;

    public boolean validarCIE10(String codigo) {
        if (codigo == null || codigo.isBlank()) return false;
        return cie10Repository.existsByCodigo(codigo.trim());
    }

    public boolean validarCUPS(String codigo) {
        if (codigo == null || codigo.isBlank()) return true; // opcional: si no hay código, puede ser error según reglas
        return cupsRepository.existsByCodigo(codigo.trim());
    }
}