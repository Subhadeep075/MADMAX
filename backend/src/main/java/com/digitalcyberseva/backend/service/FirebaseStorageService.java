package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.FileUploadResult;
import com.digitalcyberseva.backend.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO: Firebase alternative storage implementation.
 *
 * To enable:
 * 1. Add Firebase Admin SDK configuration in config package.
 * 2. Implement upload/delete logic for Firebase Storage bucket.
 * 3. Register this implementation conditionally with storage.provider=firebase.
 */
// @Service
public class FirebaseStorageService implements StorageService {

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String folder) {
        throw new BadRequestException("Firebase storage is not configured yet");
    }

    @Override
    public void deleteFile(String storagePublicId) {
        throw new BadRequestException("Firebase storage is not configured yet");
    }

    @Override
    public String getFileUrl(String storagePublicId) {
        throw new BadRequestException("Firebase storage is not configured yet");
    }
}
