package com.digitalcyberseva.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "customer_notifications",
        indexes = {
                @Index(name = "idx_customer_notifications_user", columnList = "user_id"),
                @Index(name = "idx_customer_notifications_created", columnList = "created_at")
        }
)
@Getter
@Setter
public class CustomerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_id")
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CustomerNotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "read_status", nullable = false)
    private Boolean readStatus = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (readStatus == null) {
            readStatus = false;
        }
    }
}
