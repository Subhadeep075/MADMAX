package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.entity.AuditLog;
import com.digitalcyberseva.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String entityType, Long entityId, Long performedByUserId, String remarks) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setPerformedByUserId(performedByUserId);
        auditLog.setRemarks(remarks);
        auditLogRepository.save(auditLog);
    }
}
