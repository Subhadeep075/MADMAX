package com.digitalcyberseva.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDeletionDecisionRequest {

    @Size(max = 2000, message = "Remarks can be up to 2000 characters")
    private String remarks;
}
