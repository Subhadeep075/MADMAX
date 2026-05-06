package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.ServiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ServiceResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String requiredDocumentsJson;
    private String applicantFieldsJson;
    private BigDecimal govtFee;
    private BigDecimal serviceFee;
    private BigDecimal totalFee;
    private ServiceStatus status;
    private LocalDate openDate;
    private LocalDate closeDate;
}
