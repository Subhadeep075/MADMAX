package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.AdminPastRecordResponse;
import com.digitalcyberseva.backend.entity.ApplicationRequest;
import com.digitalcyberseva.backend.entity.Payment;
import com.digitalcyberseva.backend.entity.RequestDocument;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.exception.ResourceNotFoundException;
import com.digitalcyberseva.backend.repository.ApplicationRequestRepository;
import com.digitalcyberseva.backend.repository.PaymentRepository;
import com.digitalcyberseva.backend.repository.RequestDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PastRecordService {

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApplicationRequestRepository applicationRequestRepository;
    private final PaymentRepository paymentRepository;
    private final RequestDocumentRepository requestDocumentRepository;
    private final StorageService storageService;
    private final AuditLogService auditLogService;
    private final CustomerNotificationService customerNotificationService;

    @Value("${records.retention-days:30}")
    private long retentionDays;

    @Value("${records.archive.directory:archives/past-records}")
    private String archiveDirectory;

    @Transactional(readOnly = true)
    public List<AdminPastRecordResponse> getPastRecords(LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveDateRange(fromDate, toDate);
        List<ApplicationRequest> requests = findRequests(range.fromDateTime(), range.toDateTime());
        return toPastRecordResponses(requests);
    }

    @Transactional
    public CsvExport generateCsv(LocalDate fromDate, LocalDate toDate, Long adminUserId) {
        DateRange range = resolveDateRange(fromDate, toDate);
        List<ApplicationRequest> requests = findRequests(range.fromDateTime(), range.toDateTime());
        markRequestsAsArchived(requests, "MANUAL_DOWNLOAD");
        List<AdminPastRecordResponse> records = toPastRecordResponses(requests);
        byte[] csvContent = buildCsv(records);

        String fileName = "past-records-"
                + range.fromDate().format(FILE_DATE_FORMAT)
                + "-to-"
                + range.toDate().format(FILE_DATE_FORMAT)
                + ".csv";

        auditLogService.log(
                "PAST_RECORDS_EXPORTED",
                "ApplicationRequest",
                0L,
                adminUserId,
                "from=" + range.fromDate() + ", to=" + range.toDate() + ", rows=" + records.size()
        );

        return new CsvExport(fileName, csvContent, records.size());
    }

    @Transactional
    public void archiveExpiredDayRecords() {
        LocalDate targetDate = LocalDate.now().minusDays(retentionDays);
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(LocalTime.MAX);

        List<ApplicationRequest> requests = findRequests(start, end);
        List<AdminPastRecordResponse> records = toPastRecordResponses(requests);
        if (records.isEmpty()) {
            return;
        }

        Path directory = Paths.get(archiveDirectory);
        Path filePath = directory.resolve("past-records-" + targetDate.format(FILE_DATE_FORMAT) + ".csv");

        try {
            Files.createDirectories(directory);
            if (Files.exists(filePath)) {
                markRequestsAsArchived(requests, "AUTO_DAILY_ARCHIVE");
                return;
            }
            Files.write(filePath, buildCsv(records), StandardOpenOption.CREATE_NEW);
            markRequestsAsArchived(requests, "AUTO_DAILY_ARCHIVE");
            auditLogService.log(
                    "PAST_RECORDS_ARCHIVED",
                    "ApplicationRequest",
                    0L,
                    null,
                    "date=" + targetDate + ", rows=" + records.size() + ", file=" + filePath.toAbsolutePath()
            );
        } catch (IOException ex) {
            log.error("Failed to archive past records for date {}", targetDate, ex);
        }
    }

    @Transactional
    public void autoDeleteArchivedPastRecords() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(Math.max(1, retentionDays));
        List<ApplicationRequest> requests = applicationRequestRepository
                .findByCreatedAtBeforeAndArchivedAtIsNotNullOrderByCreatedAtAsc(threshold);
        if (requests.isEmpty()) {
            return;
        }

        for (ApplicationRequest request : requests) {
            deleteRequestWithFiles(request, null, "PAST_RECORD_AUTO_DELETED");
        }
    }

    @Transactional
    public void deletePastRecord(Long requestId, Long adminUserId) {
        ApplicationRequest request = applicationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Past record not found"));

        if (request.getArchivedAt() == null) {
            throw new BadRequestException(
                    "Please download/archive this record first. For transparency, deletion is allowed only after archive.");
        }

        deleteRequestWithFiles(request, adminUserId, "PAST_RECORD_DELETED_MANUALLY");
    }

    private List<ApplicationRequest> findRequests(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        return applicationRequestRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(fromDateTime, toDateTime);
    }

    private List<AdminPastRecordResponse> toPastRecordResponses(List<ApplicationRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ApplicationRequest::getId)
                .toList();

        Map<Long, Integer> documentCountByRequestId = buildDocumentCountMap(requestIds);
        Map<Long, Payment> latestPaymentByRequestId = buildLatestPaymentMap(requestIds);

        return requests.stream()
                .map(request -> {
                    Payment latestPayment = latestPaymentByRequestId.get(request.getId());
                    return AdminPastRecordResponse.builder()
                            .requestId(request.getId())
                            .trackingId(request.getTrackingId())
                            .customerName(request.getCustomer().getName())
                            .customerMobile(request.getCustomer().getMobile())
                            .customerEmail(request.getCustomer().getEmail())
                            .serviceTitle(request.getService().getTitle())
                            .categoryName(request.getService().getCategory().getName())
                            .requestStatus(request.getStatus())
                            .paymentStatus(request.getPaymentStatus())
                            .govtFee(request.getService().getGovtFee())
                            .serviceFee(request.getService().getServiceFee())
                            .totalAmount(request.getTotalAmount())
                            .latestPaymentMethod(latestPayment != null ? latestPayment.getMethod() : null)
                            .latestUpiTransactionId(latestPayment != null ? latestPayment.getUpiTransactionId() : null)
                            .documentCount(documentCountByRequestId.getOrDefault(request.getId(), 0))
                            .remarks(request.getRemarks())
                            .archivedAt(request.getArchivedAt())
                            .archiveSource(request.getArchiveSource())
                            .createdAt(request.getCreatedAt())
                            .updatedAt(request.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    private Map<Long, Integer> buildDocumentCountMap(List<Long> requestIds) {
        List<RequestDocument> documents = requestDocumentRepository.findByRequestIdIn(requestIds);
        if (documents.isEmpty()) {
            return Map.of();
        }

        return documents.stream()
                .collect(Collectors.groupingBy(
                        document -> document.getRequest().getId(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private Map<Long, Payment> buildLatestPaymentMap(List<Long> requestIds) {
        List<Payment> payments = paymentRepository.findByRequestIdIn(requestIds);
        if (payments.isEmpty()) {
            return Map.of();
        }

        Map<Long, Payment> latestPaymentByRequestId = new HashMap<>();
        for (Payment payment : payments) {
            Long requestId = payment.getRequest().getId();
            Payment existing = latestPaymentByRequestId.get(requestId);
            if (existing == null || payment.getCreatedAt().isAfter(existing.getCreatedAt())) {
                latestPaymentByRequestId.put(requestId, payment);
            }
        }
        return latestPaymentByRequestId;
    }

    private void markRequestsAsArchived(List<ApplicationRequest> requests, String archiveSource) {
        if (requests.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean changed = false;
        for (ApplicationRequest request : requests) {
            if (request.getArchivedAt() == null) {
                request.setArchivedAt(now);
                request.setArchiveSource(archiveSource);
                changed = true;
            } else if (!StringUtils.hasText(request.getArchiveSource())) {
                request.setArchiveSource(archiveSource);
                changed = true;
            }
        }

        if (changed) {
            applicationRequestRepository.saveAll(requests);
        }
    }

    private void deleteRequestWithFiles(ApplicationRequest request, Long adminUserId, String auditAction) {
        Long requestId = request.getId();

        List<RequestDocument> documents = requestDocumentRepository.findByRequestIdOrderByUploadedAtDesc(requestId);
        for (RequestDocument document : documents) {
            storageService.deleteFile(document.getStoragePublicId());
        }

        List<Payment> payments = paymentRepository.findByRequestIdIn(List.of(requestId));
        for (Payment payment : payments) {
            if (StringUtils.hasText(payment.getPaymentProofStorageId())) {
                storageService.deleteFile(payment.getPaymentProofStorageId());
            }
        }

        if (!documents.isEmpty()) {
            requestDocumentRepository.deleteAllInBatch(documents);
        }
        if (!payments.isEmpty()) {
            paymentRepository.deleteAllInBatch(payments);
        }
        customerNotificationService.deleteByRequestIds(List.of(requestId));
        applicationRequestRepository.delete(request);

        auditLogService.log(
                auditAction,
                "ApplicationRequest",
                requestId,
                adminUserId,
                "Request deleted after archive. Keep deletion only for valid operational/compliance reasons."
        );
    }

    private DateRange resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        long safeRetentionDays = Math.max(1, retentionDays);
        LocalDate earliestAllowedDate = today.minusDays(safeRetentionDays - 1);

        LocalDate resolvedFrom = fromDate;
        LocalDate resolvedTo = toDate;

        if (resolvedFrom == null && resolvedTo == null) {
            resolvedFrom = earliestAllowedDate;
            resolvedTo = today;
        } else if (resolvedFrom == null) {
            resolvedFrom = resolvedTo.minusDays(safeRetentionDays - 1);
        } else if (resolvedTo == null) {
            resolvedTo = resolvedFrom.plusDays(safeRetentionDays - 1);
        }

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new BadRequestException("fromDate must be before or equal to toDate");
        }
        if (resolvedTo.isAfter(today)) {
            throw new BadRequestException("Future dates are not allowed");
        }
        if (resolvedFrom.isBefore(earliestAllowedDate)) {
            throw new BadRequestException("Records older than " + safeRetentionDays + " days are not available in this view");
        }

        long inclusiveDays = ChronoUnit.DAYS.between(resolvedFrom, resolvedTo) + 1;
        if (inclusiveDays > safeRetentionDays) {
            throw new BadRequestException("Please select up to " + safeRetentionDays + " days at a time");
        }

        return new DateRange(
                resolvedFrom,
                resolvedTo,
                resolvedFrom.atStartOfDay(),
                resolvedTo.atTime(LocalTime.MAX)
        );
    }

    private byte[] buildCsv(List<AdminPastRecordResponse> records) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Request ID,Tracking ID,Created At,Updated At,Customer Name,Customer Mobile,Customer Email,")
                .append("Service,Category,Request Status,Payment Status,Govt Fee,Service Fee,Total Amount,")
                .append("Latest Payment Method,Latest UPI Transaction ID,Document Count,Archived At,Archive Source,Remarks")
                .append('\n');

        for (AdminPastRecordResponse record : records) {
            csv.append(toCsvCell(record.getRequestId()))
                    .append(',')
                    .append(toCsvCell(record.getTrackingId()))
                    .append(',')
                    .append(toCsvCell(formatDateTime(record.getCreatedAt())))
                    .append(',')
                    .append(toCsvCell(formatDateTime(record.getUpdatedAt())))
                    .append(',')
                    .append(toCsvCell(record.getCustomerName()))
                    .append(',')
                    .append(toCsvCell(record.getCustomerMobile()))
                    .append(',')
                    .append(toCsvCell(record.getCustomerEmail()))
                    .append(',')
                    .append(toCsvCell(record.getServiceTitle()))
                    .append(',')
                    .append(toCsvCell(record.getCategoryName()))
                    .append(',')
                    .append(toCsvCell(record.getRequestStatus()))
                    .append(',')
                    .append(toCsvCell(record.getPaymentStatus()))
                    .append(',')
                    .append(toCsvCell(record.getGovtFee()))
                    .append(',')
                    .append(toCsvCell(record.getServiceFee()))
                    .append(',')
                    .append(toCsvCell(record.getTotalAmount()))
                    .append(',')
                    .append(toCsvCell(record.getLatestPaymentMethod()))
                    .append(',')
                    .append(toCsvCell(record.getLatestUpiTransactionId()))
                    .append(',')
                    .append(toCsvCell(record.getDocumentCount()))
                    .append(',')
                    .append(toCsvCell(formatDateTime(record.getArchivedAt())))
                    .append(',')
                    .append(toCsvCell(record.getArchiveSource()))
                    .append(',')
                    .append(toCsvCell(record.getRemarks()))
                    .append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DATE_TIME_FORMAT);
    }

    private String toCsvCell(Object value) {
        if (value == null) {
            return "";
        }

        String text = value instanceof BigDecimal amount ? amount.toPlainString() : String.valueOf(value);
        if (!StringUtils.hasText(text)) {
            return "";
        }

        String escaped = text.replace("\"", "\"\"");
        boolean needsQuotes = escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r");
        if (needsQuotes) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    public record CsvExport(String fileName, byte[] content, int rowCount) {}

    private record DateRange(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime
    ) {}
}
