package com.digitalcyberseva.backend.config;

import com.digitalcyberseva.backend.entity.*;
import com.digitalcyberseva.backend.repository.ServiceCategoryRepository;
import com.digitalcyberseva.backend.repository.ServiceRepository;
import com.digitalcyberseva.backend.repository.ShopRepository;
import com.digitalcyberseva.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceCategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;
    private final ShopRepository shopRepository;

    @Bean
    CommandLineRunner seedInitialData() {
        return args -> {
            Shop defaultShop = ensureDefaultShop();
            seedAdmin(defaultShop);
            seedCategoriesAndServices(defaultShop);
        };
    }

    private Shop ensureDefaultShop() {
        return shopRepository.findAll().stream().findFirst().orElseGet(() -> {
            Shop shop = new Shop();
            shop.setName("Digital Cyber Seva Main");
            shop.setOwnerName("System Owner");
            shop.setMobile("9000000000");
            shop.setAddress("Main Branch");
            shop.setActive(true);
            return shopRepository.save(shop);
        });
    }

    private void seedAdmin(Shop shop) {
        String adminEmail = "admin@digitalcyberseva.com";
        String adminMobile = "9999999999";

        if (userRepository.existsByEmail(adminEmail) || userRepository.existsByMobile(adminMobile)) {
            return;
        }

        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail(adminEmail);
        admin.setMobile(adminMobile);
        admin.setAddress("HQ");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ADMIN);
        admin.setShop(shop);
        userRepository.save(admin);
    }

    private void seedCategoriesAndServices(Shop shop) {
        if (categoryRepository.count() == 0) {
            ServiceCategory documents = new ServiceCategory();
            documents.setName("Documents");
            documents.setIconName("document-text-outline");
            documents.setDisplayOrder(1);
            documents.setActive(true);
            documents.setShop(shop);
            documents = categoryRepository.save(documents);

            ServiceCategory certificates = new ServiceCategory();
            certificates.setName("Certificates");
            certificates.setIconName("ribbon-outline");
            certificates.setDisplayOrder(2);
            certificates.setActive(true);
            certificates.setShop(shop);
            certificates = categoryRepository.save(certificates);

            ServiceCategory registrations = new ServiceCategory();
            registrations.setName("Registrations");
            registrations.setIconName("clipboard-outline");
            registrations.setDisplayOrder(3);
            registrations.setActive(true);
            registrations.setShop(shop);
            registrations = categoryRepository.save(registrations);

            if (serviceRepository.count() == 0) {
                Service panService = new Service();
                panService.setCategory(documents);
                panService.setTitle("PAN Card Apply");
                panService.setDescription("New PAN card application with document verification.");
                panService.setRequiredDocumentsJson("[\"Aadhaar Card\", \"Photo\", \"Mobile Number\"]");
                panService.setApplicantFieldsJson("[\"Applicant Name\", \"Applicant Mobile\", \"Applicant Email\", \"Applicant Address\", \"Father Name\"]");
                panService.setGovtFee(new BigDecimal("107.00"));
                panService.setServiceFee(new BigDecimal("60.00"));
                panService.setStatus(ServiceStatus.OPEN);
                panService.setOpenDate(LocalDate.now().minusDays(2));
                panService.setShop(shop);
                serviceRepository.save(panService);

                Service casteCertificate = new Service();
                casteCertificate.setCategory(certificates);
                casteCertificate.setTitle("Caste Certificate");
                casteCertificate.setDescription("Application and tracking support for caste certificate.");
                casteCertificate.setRequiredDocumentsJson("[\"Aadhaar Card\", \"Ration Card\", \"Photo\"]");
                casteCertificate.setApplicantFieldsJson("[\"Applicant Name\", \"Applicant Mobile\", \"Applicant Address\", \"Father Name\", \"Village\"]");
                casteCertificate.setGovtFee(new BigDecimal("20.00"));
                casteCertificate.setServiceFee(new BigDecimal("80.00"));
                casteCertificate.setStatus(ServiceStatus.OPEN);
                casteCertificate.setOpenDate(LocalDate.now().minusDays(1));
                casteCertificate.setShop(shop);
                serviceRepository.save(casteCertificate);

                Service voterUpdate = new Service();
                voterUpdate.setCategory(registrations);
                voterUpdate.setTitle("Voter ID Update");
                voterUpdate.setDescription("Correction and update assistance for voter card details.");
                voterUpdate.setRequiredDocumentsJson("[\"Existing Voter ID\", \"Address Proof\", \"Photo\"]");
                voterUpdate.setApplicantFieldsJson("[\"Applicant Name\", \"Applicant Mobile\", \"Applicant Address\", \"EPIC Number\"]");
                voterUpdate.setGovtFee(new BigDecimal("0.00"));
                voterUpdate.setServiceFee(new BigDecimal("50.00"));
                voterUpdate.setStatus(ServiceStatus.COMING_SOON);
                voterUpdate.setOpenDate(LocalDate.now().plusDays(7));
                voterUpdate.setShop(shop);
                serviceRepository.save(voterUpdate);
            }
        }
    }
}
