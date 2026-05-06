package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.ServiceResponse;
import com.digitalcyberseva.backend.dto.ServiceUpsertRequest;
import com.digitalcyberseva.backend.entity.Service;
import com.digitalcyberseva.backend.entity.ServiceCategory;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.exception.ResourceNotFoundException;
import com.digitalcyberseva.backend.repository.ApplicationRequestRepository;
import com.digitalcyberseva.backend.repository.ServiceCategoryRepository;
import com.digitalcyberseva.backend.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceManagementService {
    private static final List<String> DEFAULT_APPLICANT_FIELDS = List.of(
            "Applicant Name",
            "Applicant Mobile",
            "Applicant Email",
            "Applicant Address"
    );

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ApplicationRequestRepository applicationRequestRepository;
    private final ResponseMapper responseMapper;

    public ServiceResponse create(ServiceUpsertRequest request) {
        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Service service = new Service();
        apply(service, request, category);

        return responseMapper.toServiceResponse(serviceRepository.save(service));
    }

    public ServiceResponse update(Long id, ServiceUpsertRequest request) {
        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        apply(service, request, category);
        return responseMapper.toServiceResponse(serviceRepository.save(service));
    }

    public void delete(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        long linkedRequests = applicationRequestRepository.countByServiceId(id);
        if (linkedRequests > 0) {
            throw new BadRequestException("Cannot delete service with existing requests. Close the service instead.");
        }

        serviceRepository.delete(service);
    }

    private void apply(Service service, ServiceUpsertRequest request, ServiceCategory category) {
        service.setCategory(category);
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setRequiredDocumentsJson(request.getRequiredDocumentsJson());
        service.setApplicantFieldsJson(normalizeApplicantFieldsJson(request.getApplicantFieldsJson()));
        service.setGovtFee(request.getGovtFee());
        service.setServiceFee(request.getServiceFee());
        service.setStatus(request.getStatus());
        service.setOpenDate(request.getOpenDate());
        service.setCloseDate(request.getCloseDate());
    }

    private String normalizeApplicantFieldsJson(String raw) {
        if (!StringUtils.hasText(raw)) {
            return toJsonArray(DEFAULT_APPLICANT_FIELDS);
        }

        String trimmed = raw.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed;
        }

        List<String> fields = Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        if (fields.isEmpty()) {
            return toJsonArray(DEFAULT_APPLICANT_FIELDS);
        }

        return toJsonArray(fields);
    }

    private String toJsonArray(List<String> values) {
        String joined = values.stream()
                .map(value -> "\"" + value.replace("\"", "\\\"") + "\"")
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        return "[" + joined + "]";
    }
}
