package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.CategoryDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto.CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", categoryService.getAllCategories()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryDto.CategoryResponse>>> getActiveCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllActiveCategories(), "Active categories fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto.CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Category fetched successfully", categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto.CategoryResponse>> createCategory(@Valid @RequestBody CategoryDto.CategoryRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Category created successfully", categoryService.createCategory(request)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto.CategoryResponse>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryDto.CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}
