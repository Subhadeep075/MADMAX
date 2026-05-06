package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.ApplicationRequestStatus;
import com.digitalcyberseva.backend.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApplicationRequestResponse {
    private Long id;
    private String trackingId;
    private Long customerId;
    private String customerName;
    private ServiceResponse service;
    private ApplicationRequestStatus status;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RequestDocumentResponse> documents;
    private PaymentResponse latestPayment;
}
