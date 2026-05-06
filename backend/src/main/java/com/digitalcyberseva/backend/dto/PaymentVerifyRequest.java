package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.PaymentStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentVerifyRequest {

    @NotNull(message = "status is required")
    private PaymentStatus status;

    private String remarks;

    @AssertTrue(message = "status must be PAID or UNPAID")
    public boolean isAllowedStatus() {
        if (status == null) {
            return true;
        }
        return status == PaymentStatus.PAID || status == PaymentStatus.UNPAID;
    }
}
