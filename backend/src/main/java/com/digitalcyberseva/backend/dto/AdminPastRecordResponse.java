package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.ApplicationRequestStatus;
import com.digitalcyberseva.backend.entity.PaymentMethod;
import com.digitalcyberseva.backend.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminPastRecordResponse {
    private Long requestId;
    private String trackingId;
    private String customerName;
    private String customerMobile;
    private String customerEmail;
    private String serviceTitle;
    private String categoryName;
    private ApplicationRequestStatus requestStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal govtFee;
    private BigDecimal serviceFee;
    private BigDecimal totalAmount;
    private PaymentMethod latestPaymentMethod;
    private String latestUpiTransactionId;
    private Integer documentCount;
    private String remarks;
    private LocalDateTime archivedAt;
    private String archiveSource;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
