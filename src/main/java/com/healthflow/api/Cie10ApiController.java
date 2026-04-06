package com.healthflow.api;

import com.healthflow.domain.DiagnosticoCIE10;
import com.healthflow.repo.DiagnosticoCIE10Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cie10")
public class Cie10ApiController {

    private final DiagnosticoCIE10Repository repository;

    public Cie10ApiController(DiagnosticoCIE10Repository repository) {
        this.repository = repository;
    }

    /**
     * Endpoint para búsqueda de diagnósticos CIE-10 por código o descripción.
     * Ejemplo: GET /api/cie10/search?query=hipert
     */
    @GetMapping("/search")
    public List<DiagnosticoCIE10> search(@RequestParam("query") String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return repository.searchByCodeOrDescription(query, PageRequest.of(0, 10));
    }
}