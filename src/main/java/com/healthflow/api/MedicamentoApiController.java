package com.healthflow.api;

import com.healthflow.domain.Medicamento;
import com.healthflow.repo.MedicamentoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoApiController {

    private final MedicamentoRepository repository;

    public MedicamentoApiController(MedicamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/search")
    public List<Medicamento> search(@RequestParam("query") String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return repository.searchByNombreGenerico(query, PageRequest.of(0, 15));
    }
}