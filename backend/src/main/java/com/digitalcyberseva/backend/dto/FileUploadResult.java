package com.digitalcyberseva.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadResult {
    private String fileUrl;
    private String storagePublicId;
}
