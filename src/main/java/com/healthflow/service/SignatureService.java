package com.healthflow.service;

import com.healthflow.domain.Professional;
import com.healthflow.repo.ProfessionalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class SignatureService {

    private final Path uploadPath;
    private final ProfessionalRepository professionalRepository;

    public SignatureService(@Value("${healthflow.upload-dir:uploads}") String uploadDir,
                            ProfessionalRepository professionalRepository) {
        this.uploadPath = Paths.get(uploadDir, "firmas").toAbsolutePath().normalize();
        this.professionalRepository = professionalRepository;
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de firmas: " + uploadPath, e);
        }
    }

    public String saveSignature(MultipartFile file, Professional professional) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) extension = originalFilename.substring(dotIndex);
        String uniqueFilename = "firma_" + professional.getId().toString() + extension;
        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath);
        String filePath = targetPath.toString();
        professional.setFirmaUrl(filePath);
        professionalRepository.save(professional);
        return filePath;
    }
}