package com.digitalcyberseva.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CenterInfoResponse {
    private String centerName;
    private String mobile;
    private String whatsappNumber;
    private String address;
    private String workingHours;
}
