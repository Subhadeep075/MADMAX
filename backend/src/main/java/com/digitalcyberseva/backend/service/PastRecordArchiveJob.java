package com.digitalcyberseva.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PastRecordArchiveJob {

    private final PastRecordService pastRecordService;

    @Scheduled(
            cron = "${records.archive.cron:0 15 3 * * *}",
            zone = "${records.archive.zone:Asia/Kolkata}"
    )
    public void archiveExpiredRecords() {
        try {
            pastRecordService.archiveExpiredDayRecords();
        } catch (Exception ex) {
            log.error("Failed to archive expired past records", ex);
        }
    }
}
