package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Service
public class FileValidationService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpg",
            "image/jpeg",
            "image/png",
            "application/pdf"
    );
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpg",
            "image/jpeg",
            "image/png"
    );

    public void validateSupportedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type. Allowed: jpg, jpeg, png, pdf");
        }

        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType)) {
            String normalizedContentType = contentType.toLowerCase(Locale.ROOT).trim();
            if (!ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
                throw new BadRequestException("Unsupported content type. Allowed: jpg, jpeg, png, pdf");
            }
        }
    }

    public void validateSupportedImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported image type. Allowed: jpg, jpeg, png");
        }

        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType)) {
            String normalizedContentType = contentType.toLowerCase(Locale.ROOT).trim();
            if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(normalizedContentType)) {
                throw new BadRequestException("Unsupported image content type. Allowed: jpg, jpeg, png");
            }
        }
    }

    private String extractExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new BadRequestException("File extension is required");
        }

        return filename.substring(filename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
