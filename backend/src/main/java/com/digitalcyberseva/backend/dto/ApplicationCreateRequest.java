package com.digitalcyberseva.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationCreateRequest {

    @NotNull(message = "serviceId is required")
    private Long serviceId;

    private String remarks;
}
