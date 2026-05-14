package com.healthflow.web;

import com.healthflow.api.dto.CatalogoExamenDTO;
import com.healthflow.api.dto.PerfilClinicoDTO;
import com.healthflow.service.CatalogoExamenService;
import com.healthflow.service.PerfilClinicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalogo-examenes")
public class CatalogoExamenController {

    private final CatalogoExamenService catalogoExamenService;
    private final PerfilClinicoService perfilClinicoService;

    public CatalogoExamenController(CatalogoExamenService catalogoExamenService,
                                    PerfilClinicoService perfilClinicoService) {
        this.catalogoExamenService = catalogoExamenService;
        this.perfilClinicoService = perfilClinicoService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CatalogoExamenDTO>> buscar(@RequestParam("q") String texto) {
        return ResponseEntity.ok(catalogoExamenService.buscarPorTexto(texto));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<CatalogoExamenDTO>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(catalogoExamenService.listarPorCategoria(categoria));
    }

    @GetMapping("/perfiles")
    public ResponseEntity<List<PerfilClinicoDTO>> listarPerfiles() {
        return ResponseEntity.ok(perfilClinicoService.listarTodos());
    }

    @GetMapping("/perfiles/{id}")
    public ResponseEntity<PerfilClinicoDTO> obtenerPerfil(@PathVariable UUID id) {
        PerfilClinicoDTO dto = perfilClinicoService.obtenerConExamenes(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}