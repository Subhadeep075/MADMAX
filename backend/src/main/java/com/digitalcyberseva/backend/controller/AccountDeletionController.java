package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.AccountDeletionRequestCreate;
import com.digitalcyberseva.backend.dto.AccountDeletionStatusResponse;
import com.digitalcyberseva.backend.dto.ApiMessage;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.service.AccountDeletionService;
import com.digitalcyberseva.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account-deletion")
@RequiredArgsConstructor
public class AccountDeletionController {

    private final AccountDeletionService accountDeletionService;
    private final CurrentUserService currentUserService;

    @PostMapping("/request")
    public ApiMessage requestDeletion(@Valid @RequestBody(required = false) AccountDeletionRequestCreate request) {
        User customer = currentUserService.getCurrentUser();
        AccountDeletionRequestCreate payload = request == null ? new AccountDeletionRequestCreate() : request;
        return accountDeletionService.requestDeletion(customer, payload);
    }

    @GetMapping("/status")
    public AccountDeletionStatusResponse getDeletionStatus() {
        User customer = currentUserService.getCurrentUserAllowDeleted();
        return accountDeletionService.getMyDeletionStatus(customer);
    }
}
