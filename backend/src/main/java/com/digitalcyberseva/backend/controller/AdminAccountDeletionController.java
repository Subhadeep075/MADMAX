package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.AccountDeletionDecisionRequest;
import com.digitalcyberseva.backend.dto.AdminAccountDeletionRequestResponse;
import com.digitalcyberseva.backend.dto.ApiMessage;
import com.digitalcyberseva.backend.entity.AccountDeletionRequestStatus;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.service.AccountDeletionService;
import com.digitalcyberseva.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/account-deletion")
@RequiredArgsConstructor
public class AdminAccountDeletionController {

    private final AccountDeletionService accountDeletionService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<AdminAccountDeletionRequestResponse> getRequests(
            @RequestParam(required = false) AccountDeletionRequestStatus status
    ) {
        return accountDeletionService.getRequests(status);
    }

    @PutMapping("/{id}/approve")
    public ApiMessage approveRequest(@PathVariable Long id,
                                     @Valid @RequestBody(required = false) AccountDeletionDecisionRequest request) {
        User admin = currentUserService.getCurrentUser();
        AccountDeletionDecisionRequest payload = request == null ? new AccountDeletionDecisionRequest() : request;
        return accountDeletionService.approveRequest(admin, id, payload);
    }

    @PutMapping("/{id}/reject")
    public ApiMessage rejectRequest(@PathVariable Long id,
                                    @Valid @RequestBody(required = false) AccountDeletionDecisionRequest request) {
        User admin = currentUserService.getCurrentUser();
        AccountDeletionDecisionRequest payload = request == null ? new AccountDeletionDecisionRequest() : request;
        return accountDeletionService.rejectRequest(admin, id, payload);
    }
}
