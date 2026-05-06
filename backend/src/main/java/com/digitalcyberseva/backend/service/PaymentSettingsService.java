package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.FileUploadResult;
import com.digitalcyberseva.backend.dto.PaymentSettingsResponse;
import com.digitalcyberseva.backend.entity.PaymentSettings;
import com.digitalcyberseva.backend.entity.Role;
import com.digitalcyberseva.backend.entity.Shop;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.exception.UnauthorizedException;
import com.digitalcyberseva.backend.repository.PaymentSettingsRepository;
import com.digitalcyberseva.backend.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PaymentSettingsService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");

    private final ShopRepository shopRepository;
    private final PaymentSettingsRepository paymentSettingsRepository;
    private final StorageService storageService;
    private final FileValidationService fileValidationService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PaymentSettingsResponse getPaymentSettings() {
        Shop shop = resolveShop(null);
        PaymentSettings settings = paymentSettingsRepository.findByShopId(shop.getId()).orElse(null);
        return toResponse(settings, shop);
    }

    @Transactional
    public PaymentSettingsResponse updateUpiQr(User admin,
                                               MultipartFile qrImage,
                                               String shopUpiName,
                                               String shopUpiId,
                                               String centerName,
                                               String officialUpiId,
                                               String centerMobile,
                                               String centerWhatsappNumber,
                                               String centerAddress,
                                               String centerWorkingHours) {
        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admin can update payment settings");
        }

        Shop shop = resolveShop(admin);
        PaymentSettings settings = paymentSettingsRepository.findByShopId(shop.getId())
                .orElseGet(() -> {
                    PaymentSettings created = new PaymentSettings();
                    created.setShop(shop);
                    return created;
                });

        String previousQrStorageId = settings.getUpiQrStorageId();
        String uploadedQrStorageId = null;

        if (qrImage != null && !qrImage.isEmpty()) {
            fileValidationService.validateSupportedImageFile(qrImage);

            FileUploadResult uploadResult = storageService.uploadFile(qrImage, "digital-cyber-seva/payment-settings/upi-qr");
            uploadedQrStorageId = uploadResult.getStoragePublicId();
            settings.setUpiQrImageUrl(resolveFileUrl(uploadResult));
            settings.setUpiQrStorageId(uploadedQrStorageId);
        }
        if (shopUpiName != null) {
            settings.setUpiName(normalizeNullable(shopUpiName));
        }
        if (shopUpiId != null) {
            settings.setUpiId(normalizeNullable(shopUpiId));
        }
        if (officialUpiId != null) {
            settings.setUpiId(normalizeNullable(officialUpiId));
        }
        if (centerName != null) {
            settings.setCenterName(normalizeNullable(centerName));
        }
        if (centerMobile != null) {
            settings.setCenterMobile(normalizePhoneNullable(centerMobile, "Center mobile"));
        }
        if (centerWhatsappNumber != null) {
            settings.setCenterWhatsappNumber(normalizePhoneNullable(centerWhatsappNumber, "Center WhatsApp number"));
        }
        if (centerAddress != null) {
            settings.setCenterAddress(normalizeNullable(centerAddress));
        }
        if (centerWorkingHours != null) {
            settings.setCenterWorkingHours(normalizeNullable(centerWorkingHours));
        }

        PaymentSettings saved;
        try {
            saved = paymentSettingsRepository.save(settings);
        } catch (RuntimeException ex) {
            if (StringUtils.hasText(uploadedQrStorageId)) {
                storageService.deleteFile(uploadedQrStorageId);
            }
            throw ex;
        }

        if (StringUtils.hasText(uploadedQrStorageId)
                && StringUtils.hasText(previousQrStorageId)
                && !previousQrStorageId.equals(uploadedQrStorageId)) {
            storageService.deleteFile(previousQrStorageId);
        }

        auditLogService.log("UPI_QR_UPDATED", "PaymentSettings", saved.getId(), admin.getId(), "Shop: " + shop.getId());

        return toResponse(saved, shop);
    }

    private Shop resolveShop(User actor) {
        if (actor != null && actor.getShop() != null) {
            return actor.getShop();
        }

        return shopRepository.findFirstByActiveTrueOrderByIdAsc()
                .or(() -> shopRepository.findFirstByOrderByIdAsc())
                .orElseThrow(() -> new BadRequestException("No shop configured"));
    }

    private PaymentSettingsResponse toResponse(PaymentSettings settings, Shop shop) {
        String upiId = settings == null ? null : settings.getUpiId();
        return PaymentSettingsResponse.builder()
                .upiQrImageUrl(settings == null ? null : settings.getUpiQrImageUrl())
                .shopUpiName(settings == null ? null : settings.getUpiName())
                .shopUpiId(upiId)
                .centerName(resolveCenterName(settings, shop))
                .centerMobile(resolveCenterMobile(settings, shop))
                .centerWhatsappNumber(resolveCenterWhatsappNumber(settings, shop))
                .centerAddress(resolveCenterAddress(settings, shop))
                .centerWorkingHours(resolveCenterWorkingHours(settings))
                .officialUpiId(upiId)
                .lastUpdatedAt(settings == null ? null : settings.getUpdatedAt())
                .build();
    }

    private String resolveCenterName(PaymentSettings settings, Shop shop) {
        if (settings != null && StringUtils.hasText(settings.getCenterName())) {
            return settings.getCenterName();
        }
        return shop == null ? null : shop.getName();
    }

    private String resolveCenterMobile(PaymentSettings settings, Shop shop) {
        if (settings != null && StringUtils.hasText(settings.getCenterMobile())) {
            return settings.getCenterMobile();
        }
        return null;
    }

    private String resolveCenterWhatsappNumber(PaymentSettings settings, Shop shop) {
        if (settings != null && StringUtils.hasText(settings.getCenterWhatsappNumber())) {
            return settings.getCenterWhatsappNumber();
        }
        return resolveCenterMobile(settings, shop);
    }

    private String resolveCenterAddress(PaymentSettings settings, Shop shop) {
        if (settings != null && StringUtils.hasText(settings.getCenterAddress())) {
            return settings.getCenterAddress();
        }
        return shop == null ? null : shop.getAddress();
    }

    private String resolveCenterWorkingHours(PaymentSettings settings) {
        if (settings != null && StringUtils.hasText(settings.getCenterWorkingHours())) {
            return settings.getCenterWorkingHours();
        }
        return "10 AM - 7 PM";
    }

    private String resolveFileUrl(FileUploadResult uploadResult) {
        if (StringUtils.hasText(uploadResult.getFileUrl())) {
            return uploadResult.getFileUrl();
        }
        return storageService.getFileUrl(uploadResult.getStoragePublicId());
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizePhoneNullable(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String digitsOnly = value.replaceAll("\\D", "");
        if (!PHONE_PATTERN.matcher(digitsOnly).matches()) {
            throw new BadRequestException(fieldName + " must be 10 to 15 digits");
        }
        return digitsOnly;
    }
}
