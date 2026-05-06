package com.digitalcyberseva.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthSetPinRequest {

    @NotBlank(message = "mobile is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "mobile must be 10-15 digits")
    private String mobile;

    @NotBlank(message = "oldPassword is required")
    private String oldPassword;

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "PIN must be 4 digits")
    private String pin;
}
