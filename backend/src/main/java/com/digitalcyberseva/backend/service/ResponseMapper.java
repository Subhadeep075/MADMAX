package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.*;
import com.digitalcyberseva.backend.entity.ApplicationRequest;
import com.digitalcyberseva.backend.entity.Payment;
import com.digitalcyberseva.backend.entity.RequestDocument;
import com.digitalcyberseva.backend.entity.Service;
import com.digitalcyberseva.backend.entity.ServiceCategory;
import com.digitalcyberseva.backend.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ResponseMapper {
    private static final String DEFAULT_APPLICANT_FIELDS_JSON =
            "[\"Applicant Name\",\"Applicant Mobile\",\"Applicant Email\",\"Applicant Address\"]";

    public CategoryResponse toCategoryResponse(ServiceCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .iconName(category.getIconName())
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .build();
    }

    public CustomerProfileResponse toCustomerProfileResponse(User user) {
        return CustomerProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .address(user.getAddress())
                .build();
    }

    public ServiceResponse toServiceResponse(Service service) {
        BigDecimal totalFee = service.getGovtFee().add(service.getServiceFee());
        return ServiceResponse.builder()
                .id(service.getId())
                .categoryId(service.getCategory().getId())
                .categoryName(service.getCategory().getName())
                .title(service.getTitle())
                .description(service.getDescription())
                .requiredDocumentsJson(service.getRequiredDocumentsJson())
                .applicantFieldsJson(resolveApplicantFieldsJson(service.getApplicantFieldsJson()))
                .govtFee(service.getGovtFee())
                .serviceFee(service.getServiceFee())
                .totalFee(totalFee)
                .status(service.getStatus())
                .openDate(service.getOpenDate())
                .closeDate(service.getCloseDate())
                .build();
    }

    private String resolveApplicantFieldsJson(String value) {
        if (!StringUtils.hasText(value)) {
            return DEFAULT_APPLICANT_FIELDS_JSON;
        }
        return value;
    }

    public RequestDocumentResponse toRequestDocumentResponse(RequestDocument document) {
        return RequestDocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    public PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .upiTransactionId(payment.getUpiTransactionId())
                .paymentProofUrl(payment.getPaymentProofUrl())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public ApplicationRequestResponse toApplicationRequestResponse(ApplicationRequest request,
                                                                   List<RequestDocumentResponse> documents,
                                                                   PaymentResponse latestPayment) {
        return ApplicationRequestResponse.builder()
                .id(request.getId())
                .trackingId(request.getTrackingId())
                .customerId(request.getCustomer().getId())
                .customerName(request.getCustomer().getName())
                .service(toServiceResponse(request.getService()))
                .status(request.getStatus())
                .totalAmount(request.getTotalAmount())
                .paymentStatus(request.getPaymentStatus())
                .remarks(request.getRemarks())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .documents(documents)
                .latestPayment(latestPayment)
                .build();
    }
}
