package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.CategoryResponse;
import com.digitalcyberseva.backend.dto.ServiceResponse;
import com.digitalcyberseva.backend.entity.Service;
import com.digitalcyberseva.backend.entity.ServiceCategory;
import com.digitalcyberseva.backend.exception.ResourceNotFoundException;
import com.digitalcyberseva.backend.repository.ServiceCategoryRepository;
import com.digitalcyberseva.backend.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CatalogService {

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ResponseMapper responseMapper;

    public List<ServiceResponse> getServices() {
        return serviceRepository.findAll().stream()
                .map(responseMapper::toServiceResponse)
                .toList();
    }

    public ServiceResponse getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        return responseMapper.toServiceResponse(service);
    }

    public List<CategoryResponse> getCategories() {
        List<ServiceCategory> categories = categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
        return categories.stream().map(responseMapper::toCategoryResponse).toList();
    }
}
