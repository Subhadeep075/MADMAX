package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.CustomerNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {
    List<CustomerNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<CustomerNotification> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
    void deleteByRequestIdIn(List<Long> requestIds);
}
