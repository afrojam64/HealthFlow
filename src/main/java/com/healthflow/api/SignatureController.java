package com.healthflow.api;

import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import com.healthflow.service.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class SignatureController {

    private final SignatureService signatureService;
    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;

    public SignatureController(SignatureService signatureService,
                               UserRepository userRepository,
                               ProfessionalRepository professionalRepository) {
        this.signatureService = signatureService;
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
    }

    private Professional getCurrentProfessional() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
        return professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
    }

    @PostMapping("/firma")
    public ResponseEntity<?> uploadSignature(@RequestParam("file") MultipartFile file) {
        try {
            Professional professional = getCurrentProfessional();
            String path = signatureService.saveSignature(file, professional);
            return ResponseEntity.ok(Map.of("firmaUrl", path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/firma")
    public ResponseEntity<?> getSignature() {
        Professional professional = getCurrentProfessional();
        if (professional.getFirmaUrl() == null) {
            return ResponseEntity.ok(Map.of("firmaUrl", (String) null));
        }
        return ResponseEntity.ok(Map.of("firmaUrl", professional.getFirmaUrl()));
    }
}