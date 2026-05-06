package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.PaymentSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentSettingsRepository extends JpaRepository<PaymentSettings, Long> {
    Optional<PaymentSettings> findByShopId(Long shopId);
}
