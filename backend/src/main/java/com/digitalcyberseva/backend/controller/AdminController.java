package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.*;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.service.ApplicationRequestService;
import com.digitalcyberseva.backend.service.CategoryManagementService;
import com.digitalcyberseva.backend.service.CurrentUserService;
import com.digitalcyberseva.backend.service.PastRecordService;
import com.digitalcyberseva.backend.service.ServiceManagementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplicationRequestService applicationRequestService;
    private final CategoryManagementService categoryManagementService;
    private final ServiceManagementService serviceManagementService;
    private final PastRecordService pastRecordService;
    private final CurrentUserService currentUserService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return applicationRequestService.getDashboard();
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getCategoriesForAdmin() {
        return categoryManagementService.getAllForAdmin();
    }

    @PostMapping("/categories")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryUpsertRequest request) {
        return categoryManagementService.create(request);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id,
                                           @Valid @RequestBody CategoryUpsertRequest request) {
        return categoryManagementService.update(id, request);
    }

    @DeleteMapping("/categories/{id}")
    public ApiMessage deleteCategory(@PathVariable Long id) {
        categoryManagementService.delete(id);
        return new ApiMessage("Category deleted successfully");
    }

    @PostMapping("/services")
    public ServiceResponse createService(@Valid @RequestBody ServiceUpsertRequest request) {
        return serviceManagementService.create(request);
    }

    @PutMapping("/services/{id}")
    public ServiceResponse updateService(@PathVariable Long id,
                                         @Valid @RequestBody ServiceUpsertRequest request) {
        return serviceManagementService.update(id, request);
    }

    @DeleteMapping("/services/{id}")
    public ApiMessage deleteService(@PathVariable Long id) {
        serviceManagementService.delete(id);
        return new ApiMessage("Service deleted successfully");
    }

    @GetMapping("/requests")
    public List<ApplicationRequestResponse> getAllRequests() {
        return applicationRequestService.getAllRequestsForAdmin();
    }

    @GetMapping("/past-records")
    public List<AdminPastRecordResponse> getPastRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return pastRecordService.getPastRecords(fromDate, toDate);
    }

    @GetMapping(value = "/past-records/download", produces = "text/csv")
    public ResponseEntity<byte[]> downloadPastRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        User admin = currentUserService.getCurrentUser();
        PastRecordService.CsvExport csvExport = pastRecordService.generateCsv(fromDate, toDate, admin.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvExport.fileName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvExport.content());
    }

    @DeleteMapping("/past-records/{requestId}")
    public ApiMessage deletePastRecord(@PathVariable Long requestId) {
        User admin = currentUserService.getCurrentUser();
        pastRecordService.deletePastRecord(requestId, admin.getId());
        return new ApiMessage("Record deleted successfully.");
    }

    @GetMapping("/requests/{id}")
    public ApplicationRequestResponse getRequestById(@PathVariable Long id) {
        return applicationRequestService.getRequestForAdmin(id);
    }

    @PutMapping("/requests/{id}/status")
    public ApplicationRequestResponse updateRequestStatus(@PathVariable Long id,
                                                          @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        User admin = currentUserService.getCurrentUser();
        return applicationRequestService.updateRequestStatus(admin, id, request);
    }

    @PostMapping(value = "/requests/{id}/final-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApplicationRequestResponse uploadFinalDocument(@PathVariable Long id,
                                                          @RequestParam MultipartFile file) {
        User admin = currentUserService.getCurrentUser();
        return applicationRequestService.uploadFinalDocument(admin, id, file);
    }

    @PutMapping("/requests/{id}/payment/verify")
    public ApplicationRequestResponse verifyPayment(@PathVariable Long id,
                                                    @Valid @RequestBody PaymentVerifyRequest request) {
        User admin = currentUserService.getCurrentUser();
        return applicationRequestService.verifyPayment(admin, id, request);
    }
}
