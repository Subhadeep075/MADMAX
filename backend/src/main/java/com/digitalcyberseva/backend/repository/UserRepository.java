package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    List<User> findByIsDeletedTrueAndDeletionApprovedAtBefore(LocalDateTime threshold);
}
