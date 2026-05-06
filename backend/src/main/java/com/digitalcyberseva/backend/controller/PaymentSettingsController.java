package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.PaymentSettingsResponse;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.service.CurrentUserService;
import com.digitalcyberseva.backend.service.PaymentSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentSettingsController {

    private final PaymentSettingsService paymentSettingsService;
    private final CurrentUserService currentUserService;

    @GetMapping("/payment-settings")
    public PaymentSettingsResponse getPaymentSettings() {
        return paymentSettingsService.getPaymentSettings();
    }

    @PutMapping(value = "/admin/payment-settings/upi-qr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PaymentSettingsResponse updateUpiQr(@RequestParam(required = false) MultipartFile qrImage,
                                               @RequestParam(required = false) String shopUpiName,
                                               @RequestParam(required = false) String shopUpiId,
                                               @RequestParam(required = false) String centerName,
                                               @RequestParam(required = false) String officialUpiId,
                                               @RequestParam(required = false) String centerMobile,
                                               @RequestParam(required = false) String centerWhatsappNumber,
                                               @RequestParam(required = false) String centerAddress,
                                               @RequestParam(required = false) String centerWorkingHours) {
        User admin = currentUserService.getCurrentUser();
        return paymentSettingsService.updateUpiQr(
                admin,
                qrImage,
                shopUpiName,
                shopUpiId,
                centerName,
                officialUpiId,
                centerMobile,
                centerWhatsappNumber,
                centerAddress,
                centerWorkingHours
        );
    }
}
