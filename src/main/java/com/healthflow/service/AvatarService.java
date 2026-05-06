package com.healthflow.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AvatarService {

    private final Path uploadPath;

    public AvatarService(@Value("${healthflow.avatar.upload-dir:uploads/avatars}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de avatares", e);
        }
    }

    public String guardarAvatar(UUID professionalId, MultipartFile file) throws IOException {
        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"))) {
            throw new DomainException("Solo se permiten imágenes JPG, JPEG o PNG");
        }

        // Validar tamaño (máx 2 MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new DomainException("La imagen no puede superar los 2 MB");
        }

        // Eliminar avatar anterior si existe
        eliminarAvatar(professionalId);

        // Generar nombre único
        String extension = contentType.equals("image/png") ? "png" : "jpg";
        String filename = professionalId.toString() + "." + extension;
        Path filePath = uploadPath.resolve(filename);

        // Redimensionar y guardar (ancho 200, mantener proporción)
        BufferedImage original = ImageIO.read(file.getInputStream());
        Thumbnails.of(original)
                .size(200, 200)
                .outputFormat(extension)
                .toFile(filePath.toFile());

        return "/uploads/avatars/" + filename;
    }

    public void eliminarAvatar(UUID professionalId) {
        try {
            Files.list(uploadPath)
                    .filter(p -> p.getFileName().toString().startsWith(professionalId.toString()))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignore) {}
                    });
        } catch (IOException ignore) {}
    }
}