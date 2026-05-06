package com.digitalcyberseva.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_settings")
@Getter
@Setter
public class PaymentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Column(name = "upi_qr_image_url", length = 2000)
    private String upiQrImageUrl;

    @Column(name = "upi_qr_storage_id", length = 500)
    private String upiQrStorageId;

    @Column(name = "upi_name", length = 200)
    private String upiName;

    @Column(name = "upi_id", length = 200)
    private String upiId;

    @Column(name = "center_name", length = 200)
    private String centerName;

    @Column(name = "center_mobile", length = 20)
    private String centerMobile;

    @Column(name = "center_whatsapp_number", length = 20)
    private String centerWhatsappNumber;

    @Column(name = "center_address", length = 500)
    private String centerAddress;

    @Column(name = "center_working_hours", length = 120)
    private String centerWorkingHours;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
