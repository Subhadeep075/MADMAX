package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.*;
import com.digitalcyberseva.backend.entity.*;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.exception.ResourceNotFoundException;
import com.digitalcyberseva.backend.exception.UnauthorizedException;
import com.digitalcyberseva.backend.repository.*;
import com.digitalcyberseva.backend.util.TrackingIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ApplicationRequestService {

    private final ApplicationRequestRepository requestRepository;
    private final ServiceRepository serviceRepository;
    private final RequestDocumentRepository requestDocumentRepository;
    private final PaymentRepository paymentRepository;
    private final StorageService storageService;
    private final TrackingIdGenerator trackingIdGenerator;
    private final ResponseMapper responseMapper;
    private final AuditLogService auditLogService;
    private final CustomerNotificationService customerNotificationService;

    @Transactional
    public ApplicationRequestResponse createRequest(User customer, ApplicationCreateRequest payload) {
        Service service = serviceRepository.findById(payload.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (service.getStatus() != ServiceStatus.OPEN) {
            throw new BadRequestException("Selected service is not open for new requests");
        }

        ApplicationRequest request = new ApplicationRequest();
        request.setTrackingId(trackingIdGenerator.generate());
        request.setCustomer(customer);
        request.setService(service);
        request.setStatus(ApplicationRequestStatus.PENDING);
        request.setTotalAmount(service.getGovtFee().add(service.getServiceFee()));
        request.setPaymentStatus(PaymentStatus.UNPAID);
        request.setRemarks(payload.getRemarks());
        request.setShop(service.getShop());

        ApplicationRequest saved = requestRepository.save(request);
        auditLogService.log("REQUEST_CREATED", "ApplicationRequest", saved.getId(), customer.getId(), payload.getRemarks());
        customerNotificationService.notifyRequestSubmitted(customer.getId(), saved.getId(), saved.getTrackingId());

        return buildResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationRequestResponse> getMyRequests(User customer) {
        return requestRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationRequestResponse getMyRequestById(User customer, Long requestId) {
        ApplicationRequest request = getOwnedRequest(customer.getId(), requestId);
        return buildResponse(request);
    }

    @Transactional
    public RequestDocumentResponse uploadDocument(User customer,
                                                  Long requestId,
                                                  String documentType,
                                                  MultipartFile file) {
        if (!StringUtils.hasText(documentType)) {
            throw new BadRequestException("documentType is required");
        }

        ApplicationRequest request = getOwnedRequest(customer.getId(), requestId);
        String normalizedDocumentType = documentType.trim();
        FileUploadResult uploadResult = storageService.uploadFile(file, "digital-cyber-seva/request-documents");
        String fileUrl = resolveFileUrl(uploadResult);

        RequestDocument document = requestDocumentRepository
                .findTopByRequestIdAndDocumentTypeIgnoreCaseOrderByUploadedAtDesc(requestId, normalizedDocumentType)
                .orElseGet(RequestDocument::new);

        String previousStoragePublicId = document.getId() == null ? null : document.getStoragePublicId();

        document.setRequest(request);
        document.setDocumentType(normalizedDocumentType);
        document.setFileName(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "document");
        document.setFileUrl(fileUrl);
        document.setStoragePublicId(uploadResult.getStoragePublicId());

        RequestDocument saved;
        try {
            saved = requestDocumentRepository.save(document);
        } catch (RuntimeException ex) {
            storageService.deleteFile(uploadResult.getStoragePublicId());
            throw ex;
        }

        if (StringUtils.hasText(previousStoragePublicId)
                && !previousStoragePublicId.equals(uploadResult.getStoragePublicId())) {
            storageService.deleteFile(previousStoragePublicId);
        }

        auditLogService.log("DOCUMENT_UPLOADED", "RequestDocument", saved.getId(), customer.getId(), documentType);

        return responseMapper.toRequestDocumentResponse(saved);
    }

    @Transactional
    public ApiMessage deleteDocument(User actor, Long requestId, Long documentId) {
        ApplicationRequest request = getRequestForDelete(actor, requestId);
        RequestDocument document = requestDocumentRepository.findByIdAndRequestId(documentId, requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        storageService.deleteFile(document.getStoragePublicId());
        requestDocumentRepository.delete(document);

        String actorRole = actor.getRole().name();
        auditLogService.log("DOCUMENT_DELETED", "RequestDocument", documentId, actor.getId(),
                "Deleted by " + actorRole + " for request " + request.getId());
        return new ApiMessage("Document deleted successfully");
    }

    @Transactional
    public PaymentResponse submitPaymentProof(User customer,
                                              Long requestId,
                                              PaymentMethod method,
                                              String upiTransactionId,
                                              MultipartFile paymentProofFile) {
        ApplicationRequest request = getOwnedRequest(customer.getId(), requestId);

        PaymentMethod resolvedMethod = method == null ? PaymentMethod.UPI_QR : method;
        boolean hasFile = paymentProofFile != null && !paymentProofFile.isEmpty();
        boolean hasUpiTransactionId = StringUtils.hasText(upiTransactionId);

        if (resolvedMethod == PaymentMethod.UPI_QR && !hasFile && !hasUpiTransactionId) {
            throw new BadRequestException("Provide payment proof screenshot or UPI transaction ID");
        }

        Payment payment = paymentRepository.findTopByRequestIdOrderByCreatedAtDesc(requestId)
                .orElseGet(Payment::new);

        String previousProofStorageId = payment.getId() == null ? null : payment.getPaymentProofStorageId();
        String uploadedProofStorageId = null;

        payment.setRequest(request);
        payment.setAmount(request.getTotalAmount());
        payment.setMethod(resolvedMethod);
        payment.setUpiTransactionId(hasUpiTransactionId ? upiTransactionId.trim() : null);

        if (hasFile) {
            FileUploadResult uploadResult = storageService.uploadFile(paymentProofFile, "digital-cyber-seva/payment-proofs");
            payment.setPaymentProofUrl(resolveFileUrl(uploadResult));
            payment.setPaymentProofStorageId(uploadResult.getStoragePublicId());
            uploadedProofStorageId = uploadResult.getStoragePublicId();
        } else if (resolvedMethod == PaymentMethod.CASH) {
            payment.setPaymentProofUrl(null);
            payment.setPaymentProofStorageId(null);
            payment.setUpiTransactionId(null);
        }

        payment.setStatus(PaymentStatus.PROOF_SUBMITTED);

        Payment savedPayment;
        try {
            savedPayment = paymentRepository.save(payment);
            request.setPaymentStatus(PaymentStatus.PROOF_SUBMITTED);
            requestRepository.save(request);
        } catch (RuntimeException ex) {
            if (StringUtils.hasText(uploadedProofStorageId)) {
                storageService.deleteFile(uploadedProofStorageId);
            }
            throw ex;
        }

        if (hasFile
                && StringUtils.hasText(previousProofStorageId)
                && !previousProofStorageId.equals(uploadedProofStorageId)) {
            storageService.deleteFile(previousProofStorageId);
        }
        if (!hasFile && resolvedMethod == PaymentMethod.CASH && StringUtils.hasText(previousProofStorageId)) {
            storageService.deleteFile(previousProofStorageId);
        }

        auditLogService.log("PAYMENT_PROOF_SUBMITTED", "Payment", savedPayment.getId(), customer.getId(), resolvedMethod.name());
        return responseMapper.toPaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long totalRequests = requestRepository.count();
        long pendingRequests = requestRepository.countByStatus(ApplicationRequestStatus.PENDING);
        long inProgressRequests = requestRepository.countByStatus(ApplicationRequestStatus.IN_PROGRESS)
                + requestRepository.countByStatus(ApplicationRequestStatus.DOCUMENTS_NEEDED)
                + requestRepository.countByStatus(ApplicationRequestStatus.SUBMITTED);
        long completedRequests = requestRepository.countByStatus(ApplicationRequestStatus.COMPLETED);
        long proofSubmittedPayments = requestRepository.countByPaymentStatus(PaymentStatus.PROOF_SUBMITTED);

        return AdminDashboardResponse.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .inProgressRequests(inProgressRequests)
                .completedRequests(completedRequests)
                .proofSubmittedPayments(proofSubmittedPayments)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ApplicationRequestResponse> getAllRequestsForAdmin() {
        return requestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationRequestResponse getRequestForAdmin(Long requestId) {
        ApplicationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        return buildResponse(request);
    }

    @Transactional
    public ApplicationRequestResponse updateRequestStatus(User admin,
                                                          Long requestId,
                                                          ApplicationStatusUpdateRequest payload) {
        ApplicationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        ApplicationRequestStatus previousStatus = request.getStatus();
        request.setStatus(payload.getStatus());
        if (payload.getRemarks() != null) {
            request.setRemarks(payload.getRemarks());
        }

        ApplicationRequest saved = requestRepository.save(request);
        auditLogService.log("REQUEST_STATUS_UPDATED", "ApplicationRequest", requestId, admin.getId(), payload.getStatus().name());
        if (payload.getStatus() == ApplicationRequestStatus.COMPLETED
                && previousStatus != ApplicationRequestStatus.COMPLETED) {
            customerNotificationService.notifyFinalDocumentReady(saved.getCustomer().getId(), saved.getId());
        }

        return buildResponse(saved);
    }

    @Transactional
    public ApplicationRequestResponse uploadFinalDocument(User admin, Long requestId, MultipartFile file) {
        ApplicationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        FileUploadResult uploadResult = storageService.uploadFile(file, "digital-cyber-seva/final-documents");
        String fileUrl = resolveFileUrl(uploadResult);

        List<RequestDocument> existingFinalDocuments = requestDocumentRepository
                .findByRequestIdAndDocumentTypeIgnoreCase(requestId, "FINAL_DOCUMENT");

        RequestDocument document = new RequestDocument();
        document.setRequest(request);
        document.setDocumentType("FINAL_DOCUMENT");
        document.setFileName(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "final-document");
        document.setFileUrl(fileUrl);
        document.setStoragePublicId(uploadResult.getStoragePublicId());
        try {
            requestDocumentRepository.save(document);

            request.setStatus(ApplicationRequestStatus.COMPLETED);
            requestRepository.save(request);
        } catch (RuntimeException ex) {
            storageService.deleteFile(uploadResult.getStoragePublicId());
            throw ex;
        }

        for (RequestDocument existingFinalDocument : existingFinalDocuments) {
            storageService.deleteFile(existingFinalDocument.getStoragePublicId());
        }
        if (!existingFinalDocuments.isEmpty()) {
            requestDocumentRepository.deleteAllInBatch(existingFinalDocuments);
        }

        auditLogService.log("FINAL_DOCUMENT_UPLOADED", "ApplicationRequest", requestId, admin.getId(), "Final document uploaded");
        customerNotificationService.notifyFinalDocumentReady(request.getCustomer().getId(), request.getId());

        return buildResponse(request);
    }

    @Transactional
    public ApplicationRequestResponse verifyPayment(User admin, Long requestId, PaymentVerifyRequest payload) {
        ApplicationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        Payment payment = paymentRepository.findTopByRequestIdOrderByCreatedAtDesc(requestId)
                .orElseThrow(() -> new BadRequestException("No payment proof found for this request"));

        if (payload.getStatus() == PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            request.setPaymentStatus(PaymentStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.UNPAID);
            request.setPaymentStatus(PaymentStatus.UNPAID);
        }

        if (payload.getRemarks() != null) {
            request.setRemarks(payload.getRemarks());
        }

        paymentRepository.save(payment);
        ApplicationRequest saved = requestRepository.save(request);

        auditLogService.log("PAYMENT_VERIFIED", "Payment", payment.getId(), admin.getId(),
                payload.getStatus().name());
        if (payload.getStatus() == PaymentStatus.PAID) {
            customerNotificationService.notifyPaymentVerified(saved.getCustomer().getId(), saved.getId());
        }

        return buildResponse(saved);
    }

    private ApplicationRequest getOwnedRequest(Long customerId, Long requestId) {
        ApplicationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You can access only your own requests");
        }

        return request;
    }

    private ApplicationRequest getRequestForDelete(User actor, Long requestId) {
        if (actor.getRole() == Role.ADMIN) {
            return requestRepository.findById(requestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        }
        return getOwnedRequest(actor.getId(), requestId);
    }

    private ApplicationRequestResponse buildResponse(ApplicationRequest request) {
        List<RequestDocumentResponse> documents = requestDocumentRepository.findByRequestIdOrderByUploadedAtDesc(request.getId())
                .stream()
                .map(responseMapper::toRequestDocumentResponse)
                .toList();

        Payment latestPayment = paymentRepository.findTopByRequestIdOrderByCreatedAtDesc(request.getId()).orElse(null);

        return responseMapper.toApplicationRequestResponse(
                request,
                documents,
                responseMapper.toPaymentResponse(latestPayment)
        );
    }

    private String resolveFileUrl(FileUploadResult uploadResult) {
        if (StringUtils.hasText(uploadResult.getFileUrl())) {
            return uploadResult.getFileUrl();
        }
        return storageService.getFileUrl(uploadResult.getStoragePublicId());
    }
}
