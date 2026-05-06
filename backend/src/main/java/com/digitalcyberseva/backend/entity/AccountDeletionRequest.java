package com.digitalcyberseva.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_deletion_requests")
@Getter
@Setter
public class AccountDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountDeletionRequestStatus status = AccountDeletionRequestStatus.PENDING;

    @Column(length = 2000)
    private String reason;

    @Column(name = "admin_remarks", length = 2000)
    private String adminRemarks;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by_admin_id")
    private Long processedByAdminId;

    @PrePersist
    public void onCreate() {
        requestedAt = LocalDateTime.now();
        if (status == null) {
            status = AccountDeletionRequestStatus.PENDING;
        }
    }
}
