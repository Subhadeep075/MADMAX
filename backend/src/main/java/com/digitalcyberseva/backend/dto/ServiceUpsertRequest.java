package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.ServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ServiceUpsertRequest {

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "requiredDocumentsJson is required")
    private String requiredDocumentsJson;

    private String applicantFieldsJson;

    @NotNull(message = "govtFee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "govtFee must be positive or zero")
    private BigDecimal govtFee;

    @NotNull(message = "serviceFee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "serviceFee must be positive or zero")
    private BigDecimal serviceFee;

    @NotNull(message = "status is required")
    private ServiceStatus status;

    private LocalDate openDate;
    private LocalDate closeDate;
}
