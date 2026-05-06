package com.digitalcyberseva.backend.repository;

import com.digitalcyberseva.backend.entity.RequestDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestDocumentRepository extends JpaRepository<RequestDocument, Long> {
    List<RequestDocument> findByRequestIdOrderByUploadedAtDesc(Long requestId);
    List<RequestDocument> findByRequestIdIn(List<Long> requestIds);
    List<RequestDocument> findByRequestIdAndDocumentTypeIgnoreCase(Long requestId, String documentType);
    Optional<RequestDocument> findTopByRequestIdAndDocumentTypeIgnoreCaseOrderByUploadedAtDesc(Long requestId, String documentType);
    Optional<RequestDocument> findByIdAndRequestId(Long id, Long requestId);
}
