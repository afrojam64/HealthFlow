package com.healthflow.api;

import com.healthflow.domain.Documento;
import com.healthflow.service.DocumentoService;
import com.healthflow.service.DomainException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/documentos")
public class PublicDocumentController {

    private final DocumentoService documentoService;

    public PublicDocumentController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("token") UUID token) {
        try {
            Documento doc = documentoService.getByToken(token);
            File file = new File(doc.getFilePath());
            if (!file.exists()) {
                throw new DomainException("El archivo ya no existe");
            }
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                    .body(resource);
        } catch (DomainException e) {
            throw new RuntimeException(e); // manejar con un controlador de excepciones
        }
    }
}