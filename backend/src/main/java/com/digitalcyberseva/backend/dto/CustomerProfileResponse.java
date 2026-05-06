package com.digitalcyberseva.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {
    private Long id;
    private String name;
    private String mobile;
    private String email;
    private String address;
}
