package com.digitalcyberseva.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentSettingsResponse {
    private String upiQrImageUrl;
    private String shopUpiName;
    private String shopUpiId;
    private String centerName;
    private String centerMobile;
    private String centerWhatsappNumber;
    private String centerAddress;
    private String centerWorkingHours;
    private String officialUpiId;
    private LocalDateTime lastUpdatedAt;
}
