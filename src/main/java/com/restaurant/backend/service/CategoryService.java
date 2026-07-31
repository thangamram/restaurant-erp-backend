package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.CategoryDto;
import com.restaurant.backend.entity.Category;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto.CategoryResponse createCategory(CategoryDto.CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto.CategoryResponse updateCategory(Long id, CategoryDto.CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) category.setActive(request.getActive());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto.CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto.CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto.CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }

    private CategoryDto.CategoryResponse mapToResponse(Category category) {
        CategoryDto.CategoryResponse response = new CategoryDto.CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());
        response.setActive(category.isActive());
        response.setDisplayOrder(category.getDisplayOrder());
        return response;
    }
}
