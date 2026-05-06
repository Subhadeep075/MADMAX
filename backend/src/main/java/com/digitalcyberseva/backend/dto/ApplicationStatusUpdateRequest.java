package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.ApplicationRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "status is required")
    private ApplicationRequestStatus status;

    private String remarks;
}
