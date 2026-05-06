package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.AuthLoginRequest;
import com.digitalcyberseva.backend.dto.AuthRegisterRequest;
import com.digitalcyberseva.backend.dto.AuthResponse;
import com.digitalcyberseva.backend.dto.AuthSetPinRequest;
import com.digitalcyberseva.backend.dto.ApiMessage;
import com.digitalcyberseva.backend.entity.DeletionStatus;
import com.digitalcyberseva.backend.entity.Role;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.repository.UserRepository;
import com.digitalcyberseva.backend.security.JwtService;
import com.digitalcyberseva.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern PIN_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final String CUSTOMER_PASSWORD_DISABLED = "PIN_ONLY_LOGIN_DISABLED";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(AuthRegisterRequest request) {
        String mobile = normalize(request.getMobile());
        String email = normalize(request.getEmail());
        String pin = normalize(request.getPin());

        if (userRepository.existsByMobile(mobile)) {
            throw new BadRequestException("Mobile number already exists");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        validatePin(pin);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setMobile(mobile);
        user.setAddress(normalize(request.getAddress()));
        user.setPinHash(passwordEncoder.encode(pin));
        disableCustomerPassword(user);
        user.setRole(Role.CUSTOMER);

        User saved = userRepository.save(user);
        return toAuthResponse(saved);
    }

    public AuthResponse login(AuthLoginRequest request) {
        if (StringUtils.hasText(request.getPin())) {
            return loginCustomerWithPin(request);
        }
        return loginAdminWithPassword(request);
    }

    public ApiMessage setPin(AuthSetPinRequest request) {
        String mobile = normalize(request.getMobile());
        String oldPassword = request.getOldPassword();
        String pin = normalize(request.getPin());

        validatePin(pin);

        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new BadRequestException("Invalid mobile number or password"));
        ensureAccountActive(user);

        if (user.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Only customer accounts can set PIN");
        }

        if (!StringUtils.hasText(user.getPasswordHash()) || !passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("Invalid mobile number or password");
        }

        user.setPinHash(passwordEncoder.encode(pin));
        disableCustomerPassword(user);
        userRepository.save(user);
        return new ApiMessage("PIN set successfully. Please login with mobile and PIN");
    }

    private AuthResponse loginCustomerWithPin(AuthLoginRequest request) {
        String mobile = normalize(request.getMobile());
        if (mobile == null) {
            mobile = normalize(request.getUsername());
        }
        String pin = normalize(request.getPin());

        if (!StringUtils.hasText(mobile)) {
            throw new BadRequestException("Mobile number is required");
        }

        validatePin(pin);

        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new BadRequestException("Invalid mobile number or PIN"));

        if (user.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Invalid mobile number or PIN");
        }

        ensureAccountActive(user);

        if (!StringUtils.hasText(user.getPinHash()) || !passwordEncoder.matches(pin, user.getPinHash())) {
            throw new BadRequestException("Invalid mobile number or PIN");
        }

        return toAuthResponse(user);
    }

    private AuthResponse loginAdminWithPassword(AuthLoginRequest request) {
        String identifier = normalize(request.getUsername());
        if (identifier == null) {
            identifier = normalize(request.getMobile());
        }
        String password = request.getPassword();

        if (!StringUtils.hasText(identifier) || !StringUtils.hasText(password)) {
            throw new BadRequestException("Mobile/email and password are required");
        }

        final String loginId = identifier;
        User user = userRepository.findByEmail(loginId)
                .or(() -> userRepository.findByMobile(loginId))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getRole() != Role.ADMIN
                || !StringUtils.hasText(user.getPasswordHash())
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        ensureAccountActive(user);
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        return new AuthResponse(user.getId(), user.getName(), user.getRole().name(), token);
    }

    private void ensureAccountActive(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted()) || user.getDeletionStatus() == DeletionStatus.APPROVED) {
            throw new BadRequestException("Your account is scheduled for deletion");
        }
    }

    private void validatePin(String pin) {
        if (!StringUtils.hasText(pin) || !PIN_PATTERN.matcher(pin).matches()) {
            throw new BadRequestException("PIN must be 4 digits");
        }
    }

    private void disableCustomerPassword(User user) {
        // Keeps backward compatibility when existing MySQL schema still has password_hash as NOT NULL.
        user.setPasswordHash(passwordEncoder.encode(CUSTOMER_PASSWORD_DISABLED));
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
