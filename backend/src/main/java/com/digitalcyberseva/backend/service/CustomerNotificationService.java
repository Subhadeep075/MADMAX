package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.ApiMessage;
import com.digitalcyberseva.backend.dto.CustomerNotificationResponse;
import com.digitalcyberseva.backend.entity.CustomerNotification;
import com.digitalcyberseva.backend.entity.CustomerNotificationType;
import com.digitalcyberseva.backend.exception.ResourceNotFoundException;
import com.digitalcyberseva.backend.repository.CustomerNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerNotificationService {

    private final CustomerNotificationRepository customerNotificationRepository;

    @Transactional
    public void notifyRequestSubmitted(Long userId, Long requestId, String trackingId) {
        createNotification(
                userId,
                requestId,
                CustomerNotificationType.REQUEST_SUBMITTED,
                "Your request has been submitted. Tracking ID: " + trackingId
        );
    }

    @Transactional
    public void notifyPaymentVerified(Long userId, Long requestId) {
        createNotification(
                userId,
                requestId,
                CustomerNotificationType.PAYMENT_VERIFIED,
                "Your payment has been verified."
        );
    }

    @Transactional
    public void notifyFinalDocumentReady(Long userId, Long requestId) {
        createNotification(
                userId,
                requestId,
                CustomerNotificationType.FINAL_DOCUMENT_READY,
                "Your final document is ready. Please download it from My Requests."
        );
    }

    @Transactional(readOnly = true)
    public List<CustomerNotificationResponse> getUserNotifications(Long userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return customerNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(safeLimit)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApiMessage markAsRead(Long userId, Long notificationId) {
        CustomerNotification notification = customerNotificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setReadStatus(true);
        customerNotificationRepository.save(notification);
        return new ApiMessage("Notification marked as read");
    }

    @Transactional
    public void deleteAllForUser(Long userId) {
        customerNotificationRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteByRequestIds(List<Long> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return;
        }
        customerNotificationRepository.deleteByRequestIdIn(requestIds);
    }

    private void createNotification(Long userId, Long requestId, CustomerNotificationType type, String message) {
        CustomerNotification notification = new CustomerNotification();
        notification.setUserId(userId);
        notification.setRequestId(requestId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReadStatus(false);
        customerNotificationRepository.save(notification);
    }

    private CustomerNotificationResponse toResponse(CustomerNotification notification) {
        return CustomerNotificationResponse.builder()
                .id(notification.getId())
                .requestId(notification.getRequestId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(Boolean.TRUE.equals(notification.getReadStatus()))
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
