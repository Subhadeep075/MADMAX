package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.CategoryResponse;
import com.digitalcyberseva.backend.dto.CenterInfoResponse;
import com.digitalcyberseva.backend.dto.ServiceResponse;
import com.digitalcyberseva.backend.service.CenterInfoService;
import com.digitalcyberseva.backend.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final CenterInfoService centerInfoService;

    @GetMapping("/api/services")
    public List<ServiceResponse> getServices() {
        return catalogService.getServices();
    }

    @GetMapping("/api/services/{id}")
    public ServiceResponse getServiceById(@PathVariable Long id) {
        return catalogService.getServiceById(id);
    }

    @GetMapping("/api/categories")
    public List<CategoryResponse> getCategories() {
        return catalogService.getCategories();
    }

    @GetMapping("/api/public/center-info")
    public CenterInfoResponse getCenterInfo() {
        return centerInfoService.getPublicCenterInfo();
    }
}
