package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.Service;
import com.digitalcyberseva.backend.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByStatusOrderByCreatedAtDesc(ServiceStatus status);
    long countByCategory_Id(Long categoryId);
}
