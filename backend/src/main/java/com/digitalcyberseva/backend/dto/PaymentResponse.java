package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.PaymentMethod;
import com.digitalcyberseva.backend.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private PaymentMethod method;
    private String upiTransactionId;
    private String paymentProofUrl;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
