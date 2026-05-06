package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.CenterInfoResponse;
import com.digitalcyberseva.backend.entity.PaymentSettings;
import com.digitalcyberseva.backend.entity.Shop;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.repository.PaymentSettingsRepository;
import com.digitalcyberseva.backend.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CenterInfoService {

    private final ShopRepository shopRepository;
    private final PaymentSettingsRepository paymentSettingsRepository;

    public CenterInfoResponse getPublicCenterInfo() {
        Shop shop = resolveShop();
        PaymentSettings settings = paymentSettingsRepository.findByShopId(shop.getId()).orElse(null);

        String centerName = firstNonBlank(
                settings == null ? null : settings.getCenterName(),
                shop.getName(),
                "Digital Cyber Seva"
        );
        String mobile = firstNonBlank(
                settings == null ? null : settings.getCenterMobile(),
                ""
        );
        String whatsappNumber = firstNonBlank(
                settings == null ? null : settings.getCenterWhatsappNumber(),
                ""
        );
        String address = firstNonBlank(
                settings == null ? null : settings.getCenterAddress(),
                shop.getAddress(),
                ""
        );
        String workingHours = firstNonBlank(
                settings == null ? null : settings.getCenterWorkingHours(),
                "10 AM - 7 PM"
        );

        return CenterInfoResponse.builder()
                .centerName(centerName)
                .mobile(mobile)
                .whatsappNumber(whatsappNumber)
                .address(address)
                .workingHours(workingHours)
                .build();
    }

    private Shop resolveShop() {
        return shopRepository.findFirstByActiveTrueOrderByIdAsc()
                .or(() -> shopRepository.findFirstByOrderByIdAsc())
                .orElseThrow(() -> new BadRequestException("No shop configured"));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
