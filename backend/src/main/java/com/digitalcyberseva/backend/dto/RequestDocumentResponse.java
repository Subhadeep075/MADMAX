package com.digitalcyberseva.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RequestDocumentResponse {
    private Long id;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
