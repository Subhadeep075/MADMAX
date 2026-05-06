package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.AuthLoginRequest;
import com.digitalcyberseva.backend.dto.AuthRegisterRequest;
import com.digitalcyberseva.backend.dto.AuthResponse;
import com.digitalcyberseva.backend.dto.AuthSetPinRequest;
import com.digitalcyberseva.backend.dto.ApiMessage;
import com.digitalcyberseva.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/set-pin")
    public ResponseEntity<ApiMessage> setPin(@Valid @RequestBody AuthSetPinRequest request) {
        return ResponseEntity.ok(authService.setPin(request));
    }
}
