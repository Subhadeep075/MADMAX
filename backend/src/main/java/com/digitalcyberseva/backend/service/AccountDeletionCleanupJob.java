package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDeletionCleanupJob {

    private final UserRepository userRepository;
    private final AccountDeletionCleanupService accountDeletionCleanupService;

    @Value("${account-deletion.cleanup.retention-days:7}")
    private long retentionDays;

    @Scheduled(
            cron = "${account-deletion.cleanup.cron:0 30 2 * * *}",
            zone = "${account-deletion.cleanup.zone:Asia/Kolkata}"
    )
    public void cleanupApprovedAccounts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        List<Long> userIds = userRepository.findByIsDeletedTrueAndDeletionApprovedAtBefore(threshold)
                .stream()
                .map(user -> user.getId())
                .toList();

        if (userIds.isEmpty()) {
            return;
        }

        for (Long userId : userIds) {
            try {
                accountDeletionCleanupService.permanentlyDeleteUser(userId);
            } catch (Exception ex) {
                log.error("Failed to permanently delete user {}", userId, ex);
            }
        }
    }
}
