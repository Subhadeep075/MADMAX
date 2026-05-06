package com.digitalcyberseva.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalRequests;
    private long pendingRequests;
    private long inProgressRequests;
    private long completedRequests;
    private long proofSubmittedPayments;
}
