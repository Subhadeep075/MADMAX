package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.entity.*;
import com.digitalcyberseva.backend.repository.AccountDeletionRequestRepository;
import com.digitalcyberseva.backend.repository.ApplicationRequestRepository;
import com.digitalcyberseva.backend.repository.PaymentRepository;
import com.digitalcyberseva.backend.repository.RequestDocumentRepository;
import com.digitalcyberseva.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDeletionCleanupService {

    private final UserRepository userRepository;
    private final ApplicationRequestRepository applicationRequestRepository;
    private final RequestDocumentRepository requestDocumentRepository;
    private final PaymentRepository paymentRepository;
    private final AccountDeletionRequestRepository accountDeletionRequestRepository;
    private final StorageService storageService;
    private final AuditLogService auditLogService;
    private final CustomerNotificationService customerNotificationService;

    @Transactional
    public void permanentlyDeleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        if (!Boolean.TRUE.equals(user.getIsDeleted()) || user.getDeletionStatus() != DeletionStatus.APPROVED) {
            return;
        }

        List<ApplicationRequest> requests = applicationRequestRepository.findByCustomerId(userId);
        if (!requests.isEmpty()) {
            List<Long> requestIds = requests.stream().map(ApplicationRequest::getId).toList();

            List<RequestDocument> requestDocuments = requestDocumentRepository.findByRequestIdIn(requestIds);
            requestDocuments.forEach(document -> storageService.deleteFile(document.getStoragePublicId()));

            List<Payment> payments = paymentRepository.findByRequestIdIn(requestIds);
            payments.forEach(payment -> {
                if (StringUtils.hasText(payment.getPaymentProofStorageId())) {
                    storageService.deleteFile(payment.getPaymentProofStorageId());
                }
            });

            if (!requestDocuments.isEmpty()) {
                requestDocumentRepository.deleteAllInBatch(requestDocuments);
            }
            if (!payments.isEmpty()) {
                paymentRepository.deleteAllInBatch(payments);
            }
            customerNotificationService.deleteByRequestIds(requestIds);
            applicationRequestRepository.deleteAllInBatch(requests);
        }

        List<AccountDeletionRequest> deletionRequests = accountDeletionRequestRepository.findByUserId(userId);
        if (!deletionRequests.isEmpty()) {
            accountDeletionRequestRepository.deleteAllInBatch(deletionRequests);
        }

        customerNotificationService.deleteAllForUser(userId);
        userRepository.delete(user);
        auditLogService.log("USER_PERMANENTLY_DELETED", "User", userId, null, "User data permanently removed");
    }
}
