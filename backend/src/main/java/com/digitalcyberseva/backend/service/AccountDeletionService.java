package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.*;
import com.digitalcyberseva.backend.entity.*;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.exception.ResourceNotFoundException;
import com.digitalcyberseva.backend.exception.UnauthorizedException;
import com.digitalcyberseva.backend.repository.AccountDeletionRequestRepository;
import com.digitalcyberseva.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDeletionService {

    private final AccountDeletionRequestRepository accountDeletionRequestRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ApiMessage requestDeletion(User customer, AccountDeletionRequestCreate payload) {
        if (customer.getRole() != Role.CUSTOMER) {
            throw new UnauthorizedException("Only customer account deletion requests are allowed");
        }

        if (Boolean.TRUE.equals(customer.getIsDeleted()) || customer.getDeletionStatus() == DeletionStatus.APPROVED) {
            throw new BadRequestException("Your account is already scheduled for deletion");
        }

        boolean hasPendingRequest = customer.getDeletionStatus() == DeletionStatus.PENDING
                || accountDeletionRequestRepository.existsByUserIdAndStatus(
                customer.getId(),
                AccountDeletionRequestStatus.PENDING
        );

        if (hasPendingRequest) {
            throw new BadRequestException("Your deletion request is already pending admin approval");
        }

        LocalDateTime now = LocalDateTime.now();
        customer.setDeletionStatus(DeletionStatus.PENDING);
        customer.setDeletionRequestedAt(now);
        customer.setDeletionApprovedAt(null);
        customer.setIsDeleted(false);
        userRepository.save(customer);

        AccountDeletionRequest request = new AccountDeletionRequest();
        request.setUser(customer);
        request.setStatus(AccountDeletionRequestStatus.PENDING);
        request.setReason(normalizeNullable(payload == null ? null : payload.getReason()));
        AccountDeletionRequest saved = accountDeletionRequestRepository.save(request);

        auditLogService.log(
                "ACCOUNT_DELETION_REQUESTED",
                "AccountDeletionRequest",
                saved.getId(),
                customer.getId(),
                saved.getReason()
        );
        return new ApiMessage("Your deletion request is submitted and pending admin approval.");
    }

    @Transactional(readOnly = true)
    public AccountDeletionStatusResponse getMyDeletionStatus(User user) {
        return AccountDeletionStatusResponse.builder()
                .deletionStatus(user.getDeletionStatus())
                .deletionRequestedAt(user.getDeletionRequestedAt())
                .deletionApprovedAt(user.getDeletionApprovedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminAccountDeletionRequestResponse> getRequests(AccountDeletionRequestStatus status) {
        List<AccountDeletionRequest> requests;
        if (status == null) {
            requests = accountDeletionRequestRepository.findAllByOrderByRequestedAtDesc();
        } else {
            requests = accountDeletionRequestRepository.findByStatusOrderByRequestedAtDesc(status);
        }

        return requests.stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public ApiMessage approveRequest(User admin, Long requestId, AccountDeletionDecisionRequest payload) {
        ensureAdmin(admin);

        AccountDeletionRequest request = accountDeletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Account deletion request not found"));

        if (request.getStatus() != AccountDeletionRequestStatus.PENDING) {
            throw new BadRequestException("Only pending deletion requests can be approved");
        }

        User customer = request.getUser();
        LocalDateTime now = LocalDateTime.now();

        customer.setIsDeleted(true);
        customer.setDeletionStatus(DeletionStatus.APPROVED);
        customer.setDeletionApprovedAt(now);
        userRepository.save(customer);

        request.setStatus(AccountDeletionRequestStatus.APPROVED);
        request.setProcessedAt(now);
        request.setProcessedByAdminId(admin.getId());
        request.setAdminRemarks(normalizeNullable(payload == null ? null : payload.getRemarks()));
        accountDeletionRequestRepository.save(request);

        auditLogService.log(
                "ACCOUNT_DELETION_APPROVED",
                "User",
                customer.getId(),
                admin.getId(),
                "User marked for deletion"
        );

        return new ApiMessage("Account deletion request approved. User is now scheduled for permanent deletion.");
    }

    @Transactional
    public ApiMessage rejectRequest(User admin, Long requestId, AccountDeletionDecisionRequest payload) {
        ensureAdmin(admin);

        AccountDeletionRequest request = accountDeletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Account deletion request not found"));

        if (request.getStatus() != AccountDeletionRequestStatus.PENDING) {
            throw new BadRequestException("Only pending deletion requests can be rejected");
        }

        User customer = request.getUser();
        customer.setDeletionStatus(DeletionStatus.REJECTED);
        customer.setIsDeleted(false);
        customer.setDeletionApprovedAt(null);
        userRepository.save(customer);

        String remarks = normalizeNullable(payload == null ? null : payload.getRemarks());
        request.setStatus(AccountDeletionRequestStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedByAdminId(admin.getId());
        request.setAdminRemarks(remarks);
        accountDeletionRequestRepository.save(request);

        auditLogService.log(
                "ACCOUNT_DELETION_REJECTED",
                "User",
                customer.getId(),
                admin.getId(),
                remarks
        );

        return new ApiMessage("Account deletion request rejected.");
    }

    private AdminAccountDeletionRequestResponse toAdminResponse(AccountDeletionRequest request) {
        User user = request.getUser();
        return AdminAccountDeletionRequestResponse.builder()
                .id(request.getId())
                .userId(user.getId())
                .userName(user.getName())
                .userMobile(user.getMobile())
                .userEmail(user.getEmail())
                .status(request.getStatus())
                .reason(request.getReason())
                .adminRemarks(request.getAdminRemarks())
                .requestedAt(request.getRequestedAt())
                .processedAt(request.getProcessedAt())
                .processedByAdminId(request.getProcessedByAdminId())
                .build();
    }

    private void ensureAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admin can process account deletion requests");
        }
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
