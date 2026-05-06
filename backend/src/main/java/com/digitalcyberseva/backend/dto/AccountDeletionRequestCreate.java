package com.digitalcyberseva.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDeletionRequestCreate {

    @Size(max = 2000, message = "Reason can be up to 2000 characters")
    private String reason;
}
