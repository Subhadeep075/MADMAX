package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.DeletionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccountDeletionStatusResponse {
    private DeletionStatus deletionStatus;
    private LocalDateTime deletionRequestedAt;
    private LocalDateTime deletionApprovedAt;
}
