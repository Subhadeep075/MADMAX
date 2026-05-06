package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.*;
import com.digitalcyberseva.backend.entity.PaymentMethod;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.service.ApplicationRequestService;
import com.digitalcyberseva.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final ApplicationRequestService applicationRequestService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ApplicationRequestResponse createRequest(@Valid @RequestBody ApplicationCreateRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        return applicationRequestService.createRequest(currentUser, request);
    }

    @GetMapping("/my")
    public List<ApplicationRequestResponse> getMyRequests() {
        User currentUser = currentUserService.getCurrentUser();
        return applicationRequestService.getMyRequests(currentUser);
    }

    @GetMapping("/{id}")
    public ApplicationRequestResponse getMyRequestById(@PathVariable Long id) {
        User currentUser = currentUserService.getCurrentUser();
        return applicationRequestService.getMyRequestById(currentUser, id);
    }

    @PostMapping(value = "/{requestId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RequestDocumentResponse uploadDocument(@PathVariable Long requestId,
                                                  @RequestParam String documentType,
                                                  @RequestParam MultipartFile file) {
        User currentUser = currentUserService.getCurrentUser();
        return applicationRequestService.uploadDocument(currentUser, requestId, documentType, file);
    }

    // Frontend UI should ask for user confirmation before calling delete.
    @DeleteMapping("/{requestId}/documents/{documentId}")
    public ApiMessage deleteDocument(@PathVariable Long requestId,
                                     @PathVariable Long documentId) {
        User currentUser = currentUserService.getCurrentUser();
        return applicationRequestService.deleteDocument(currentUser, requestId, documentId);
    }

    @PostMapping(value = "/{id}/payment-proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PaymentResponse submitPaymentProof(@PathVariable Long id,
                                              @RequestParam(required = false) PaymentMethod method,
                                              @RequestParam(required = false) String upiTransactionId,
                                              @RequestParam(name = "screenshot", required = false) MultipartFile screenshot,
                                              @RequestParam(name = "paymentProofFile", required = false) MultipartFile paymentProofFile) {
        User currentUser = currentUserService.getCurrentUser();
        MultipartFile finalProofFile = (screenshot != null && !screenshot.isEmpty()) ? screenshot : paymentProofFile;
        return applicationRequestService.submitPaymentProof(currentUser, id, method, upiTransactionId, finalProofFile);
    }
}
