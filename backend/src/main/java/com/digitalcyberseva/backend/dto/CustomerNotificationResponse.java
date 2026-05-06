package com.digitalcyberseva.backend.dto;

import com.digitalcyberseva.backend.entity.CustomerNotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerNotificationResponse {
    private Long id;
    private Long requestId;
    private CustomerNotificationType type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
