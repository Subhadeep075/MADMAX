package com.digitalcyberseva.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthLoginRequest {

    // Admin login supports email/mobile + password.
    private String username;

    // Customer login uses mobile + 4-digit PIN.
    private String mobile;

    private String pin;

    private String password;
}
