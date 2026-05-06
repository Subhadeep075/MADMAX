package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.CustomerNameUpdateRequest;
import com.digitalcyberseva.backend.dto.CustomerProfileResponse;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.service.CurrentUserService;
import com.digitalcyberseva.backend.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CurrentUserService currentUserService;
    private final CustomerProfileService customerProfileService;

    @GetMapping
    public CustomerProfileResponse getMyProfile() {
        User customer = currentUserService.getCurrentUser();
        return customerProfileService.getMyProfile(customer);
    }

    @PutMapping("/name")
    public CustomerProfileResponse updateMyName(@Valid @RequestBody CustomerNameUpdateRequest request) {
        User customer = currentUserService.getCurrentUser();
        return customerProfileService.updateMyName(customer, request);
    }
}
