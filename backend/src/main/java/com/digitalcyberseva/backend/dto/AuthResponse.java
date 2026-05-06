package com.digitalcyberseva.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String name;
    private String role;
    private String token;
}
