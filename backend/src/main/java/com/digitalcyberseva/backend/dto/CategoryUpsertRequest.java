package com.digitalcyberseva.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpsertRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String iconName;

    @NotNull(message = "displayOrder is required")
    private Integer displayOrder;

    @NotNull(message = "active is required")
    private Boolean active;
}
