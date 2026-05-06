package com.digitalcyberseva.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.digitalcyberseva.backend.dto.FileUploadResult;
import com.digitalcyberseva.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@org.springframework.stereotype.Service
@Primary
@RequiredArgsConstructor
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;
    private final FileValidationService fileValidationService;
    private final LocalStorageService localStorageService;

    @Value("${storage.provider:cloudinary}")
    private String storageProvider;

    @Value("${storage.fallback-to-local:true}")
    private boolean fallbackToLocal;

    @Value("${storage.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${storage.cloudinary.api-key:}")
    private String apiKey;

    @Value("${storage.cloudinary.api-secret:}")
    private String apiSecret;

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String folder) {
        if (!"cloudinary".equalsIgnoreCase(storageProvider)) {
            return localStorageService.uploadFile(file, folder);
        }
        fileValidationService.validateSupportedFile(file);

        if (!isCloudinaryConfigured()) {
            if (fallbackToLocal) {
                return localStorageService.uploadFile(file, folder);
            }
            throw new BadRequestException("Cloudinary is not configured");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", "auto")
            );
            return new FileUploadResult(
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id")
            );
        } catch (Exception ex) {
            if (fallbackToLocal) {
                return localStorageService.uploadFile(file, folder);
            }
            throw new BadRequestException("Unable to upload file");
        }
    }

    @Override
    public void deleteFile(String storagePublicId) {
        if (storagePublicId == null || storagePublicId.isBlank()) {
            return;
        }

        if (localStorageService.isLocalStorageId(storagePublicId)) {
            localStorageService.deleteFile(storagePublicId);
            return;
        }

        if (!"cloudinary".equalsIgnoreCase(storageProvider) || !isCloudinaryConfigured()) {
            if (fallbackToLocal) {
                localStorageService.deleteFile(storagePublicId);
            }
            return;
        }

        try {
            cloudinary.uploader().destroy(storagePublicId, ObjectUtils.emptyMap());
        } catch (IOException ex) {
            if (fallbackToLocal) {
                localStorageService.deleteFile(storagePublicId);
                return;
            }
            throw new BadRequestException("Unable to delete file from cloud storage");
        }
    }

    @Override
    public String getFileUrl(String storagePublicId) {
        if (localStorageService.isLocalStorageId(storagePublicId)) {
            return localStorageService.getFileUrl(storagePublicId);
        }

        if (!StringUtils.hasText(storagePublicId)) {
            return null;
        }

        if (!"cloudinary".equalsIgnoreCase(storageProvider) || !isCloudinaryConfigured()) {
            return fallbackToLocal ? localStorageService.getFileUrl(storagePublicId) : null;
        }

        return cloudinary.url()
                .secure(true)
                .generate(storagePublicId);
    }

    private boolean isCloudinaryConfigured() {
        return StringUtils.hasText(cloudName)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(apiSecret);
    }
}
