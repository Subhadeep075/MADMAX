package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    FileUploadResult uploadFile(MultipartFile file, String folder);
    void deleteFile(String storagePublicId);
    String getFileUrl(String storagePublicId);
}
