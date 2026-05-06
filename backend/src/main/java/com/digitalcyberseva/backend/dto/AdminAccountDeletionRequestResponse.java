package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.AccountDeletionRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminAccountDeletionRequestResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userMobile;
    private String userEmail;
    private AccountDeletionRequestStatus status;
    private String reason;
    private String adminRemarks;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long processedByAdminId;
}
