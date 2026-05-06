package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.dto.CategoryResponse;
import com.digitalcyberseva.backend.dto.CategoryUpsertRequest;
import com.digitalcyberseva.backend.entity.ServiceCategory;
import com.digitalcyberseva.backend.exception.BadRequestException;
import com.digitalcyberseva.backend.exception.ResourceNotFoundException;
import com.digitalcyberseva.backend.repository.ServiceCategoryRepository;
import com.digitalcyberseva.backend.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CategoryManagementService {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;
    private final ResponseMapper responseMapper;

    public List<CategoryResponse> getAllForAdmin() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(responseMapper::toCategoryResponse)
                .toList();
    }

    public CategoryResponse create(CategoryUpsertRequest request) {
        ServiceCategory category = new ServiceCategory();
        applyCategoryFields(category, request);
        return responseMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryUpsertRequest request) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        applyCategoryFields(category, request);

        return responseMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        long linkedServices = serviceRepository.countByCategory_Id(id);
        if (linkedServices > 0) {
            throw new BadRequestException("Cannot delete category with existing services. Deactivate it instead.");
        }

        try {
            categoryRepository.delete(category);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Cannot delete category because it is linked to existing data. Deactivate it instead.");
        }
    }

    private void applyCategoryFields(ServiceCategory category, CategoryUpsertRequest request) {
        category.setName(request.getName());
        category.setIconName(request.getIconName());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.getActive());
    }
}
