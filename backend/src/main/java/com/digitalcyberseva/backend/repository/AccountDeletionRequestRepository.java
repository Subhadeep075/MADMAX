package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.AccountDeletionRequest;
import com.digitalcyberseva.backend.entity.AccountDeletionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountDeletionRequestRepository extends JpaRepository<AccountDeletionRequest, Long> {
    boolean existsByUserIdAndStatus(Long userId, AccountDeletionRequestStatus status);
    Optional<AccountDeletionRequest> findTopByUserIdOrderByRequestedAtDesc(Long userId);
    List<AccountDeletionRequest> findAllByOrderByRequestedAtDesc();
    List<AccountDeletionRequest> findByStatusOrderByRequestedAtDesc(AccountDeletionRequestStatus status);
    List<AccountDeletionRequest> findByUserId(Long userId);
}
