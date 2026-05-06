package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.FileUploadResult;
import com.digitalcyberseva.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
public class LocalStorageService implements StorageService {

    private static final String LOCAL_STORAGE_PREFIX = "local:";

    private final FileValidationService fileValidationService;
    private final Path rootPath;
    private final String publicBaseUrl;

    public LocalStorageService(FileValidationService fileValidationService,
                               @Value("${storage.local.base-path:uploads}") String basePath,
                               @Value("${storage.local.public-base-url:http://localhost:8080/uploads}") String publicBaseUrl) {
        this.fileValidationService = fileValidationService;
        this.rootPath = Paths.get(basePath).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String folder) {
        fileValidationService.validateSupportedFile(file);

        String normalizedFolder = normalizeFolder(folder);
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        String relativePath = normalizedFolder + "/" + fileName;

        Path target = rootPath.resolve(relativePath).normalize();
        ensureWithinRoot(target);

        try {
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new BadRequestException("Unable to upload file");
        }

        return new FileUploadResult(buildFileUrl(relativePath), LOCAL_STORAGE_PREFIX + relativePath);
    }

    @Override
    public void deleteFile(String storagePublicId) {
        String relativePath = resolveRelativePath(storagePublicId);
        if (!StringUtils.hasText(relativePath)) {
            return;
        }

        Path filePath = rootPath.resolve(relativePath).normalize();
        ensureWithinRoot(filePath);

        try {
            Files.deleteIfExists(filePath);
            cleanupEmptyParents(filePath.getParent());
        } catch (IOException ex) {
            throw new BadRequestException("Unable to delete file from local storage");
        }
    }

    @Override
    public String getFileUrl(String storagePublicId) {
        String relativePath = resolveRelativePath(storagePublicId);
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        return buildFileUrl(relativePath);
    }

    public boolean isLocalStorageId(String storagePublicId) {
        return StringUtils.hasText(storagePublicId) && storagePublicId.startsWith(LOCAL_STORAGE_PREFIX);
    }

    private String resolveRelativePath(String storagePublicId) {
        if (!isLocalStorageId(storagePublicId)) {
            return null;
        }
        return storagePublicId.substring(LOCAL_STORAGE_PREFIX.length());
    }

    private String normalizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            throw new BadRequestException("Storage folder is required");
        }

        String normalized = folder.trim()
                .replace("\\", "/")
                .replaceAll("/+", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");

        if (!StringUtils.hasText(normalized) || normalized.contains("..")) {
            throw new BadRequestException("Invalid storage folder");
        }
        return normalized;
    }

    private String extractExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new BadRequestException("File extension is required");
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT).trim();
    }

    private String buildFileUrl(String relativePath) {
        return publicBaseUrl + "/" + relativePath;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "http://localhost:8080/uploads";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private void ensureWithinRoot(Path path) {
        if (!path.startsWith(rootPath)) {
            throw new BadRequestException("Invalid storage path");
        }
    }

    private void cleanupEmptyParents(Path path) {
        Path current = path;
        while (current != null && !current.equals(rootPath)) {
            try {
                try (Stream<Path> entries = Files.list(current)) {
                    if (entries.findAny().isPresent()) {
                        break;
                    }
                }
                Files.deleteIfExists(current);
                current = current.getParent();
            } catch (IOException ex) {
                log.debug("Skipping local folder cleanup for {}", current, ex);
                break;
            }
        }
    }
}
