package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findTopByRequestIdOrderByCreatedAtDesc(Long requestId);
    List<Payment> findByRequestIdIn(List<Long> requestIds);
}
