package com.digitalcyberseva.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PastRecordCleanupJob {

    private final PastRecordService pastRecordService;

    @Scheduled(
            cron = "${records.cleanup.cron:0 45 3 * * *}",
            zone = "${records.cleanup.zone:Asia/Kolkata}"
    )
    public void cleanupArchivedRecords() {
        try {
            pastRecordService.autoDeleteArchivedPastRecords();
        } catch (Exception ex) {
            log.error("Failed to auto-delete archived past records", ex);
        }
    }
}
