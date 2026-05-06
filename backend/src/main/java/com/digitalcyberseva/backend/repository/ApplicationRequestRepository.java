package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.ApplicationRequest;
import com.digitalcyberseva.backend.entity.ApplicationRequestStatus;
import com.digitalcyberseva.backend.entity.PaymentStatus;
import com.digitalcyberseva.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRequestRepository extends JpaRepository<ApplicationRequest, Long> {
    List<ApplicationRequest> findByCustomerOrderByCreatedAtDesc(User customer);
    List<ApplicationRequest> findByCustomerId(Long customerId);
    Optional<ApplicationRequest> findByTrackingId(String trackingId);
    List<ApplicationRequest> findAllByOrderByCreatedAtDesc();
    List<ApplicationRequest> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);
    List<ApplicationRequest> findByCreatedAtBeforeOrderByCreatedAtAsc(LocalDateTime threshold);
    List<ApplicationRequest> findByCreatedAtBeforeAndArchivedAtIsNotNullOrderByCreatedAtAsc(LocalDateTime threshold);
    long countByStatus(ApplicationRequestStatus status);
    long countByPaymentStatus(PaymentStatus paymentStatus);
    long countByServiceId(Long serviceId);
}
