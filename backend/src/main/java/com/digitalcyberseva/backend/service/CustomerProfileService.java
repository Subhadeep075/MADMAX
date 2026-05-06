package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.CustomerNameUpdateRequest;
import com.digitalcyberseva.backend.dto.CustomerProfileResponse;
import com.digitalcyberseva.backend.entity.Role;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.exception.UnauthorizedException;
import com.digitalcyberseva.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final UserRepository userRepository;
    private final ResponseMapper responseMapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public CustomerProfileResponse getMyProfile(User customer) {
        ensureCustomer(customer);
        return responseMapper.toCustomerProfileResponse(customer);
    }

    @Transactional
    public CustomerProfileResponse updateMyName(User customer, CustomerNameUpdateRequest payload) {
        ensureCustomer(customer);

        String normalizedName = normalizeName(payload.getName());
        customer.setName(normalizedName);
        User saved = userRepository.save(customer);

        auditLogService.log(
                "CUSTOMER_NAME_UPDATED",
                "User",
                saved.getId(),
                saved.getId(),
                "Customer updated profile name"
        );

        return responseMapper.toCustomerProfileResponse(saved);
    }

    private void ensureCustomer(User user) {
        if (user.getRole() != Role.CUSTOMER) {
            throw new UnauthorizedException("Only customer profile updates are allowed");
        }
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException("Name is required");
        }

        String normalized = name.trim();
        if (normalized.length() < 2 || normalized.length() > 80) {
            throw new BadRequestException("Name must be between 2 and 80 characters");
        }
        return normalized;
    }
}
