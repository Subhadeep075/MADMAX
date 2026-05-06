package com.digitalcyberseva.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String iconName;
    private Integer displayOrder;
    private Boolean active;
}
