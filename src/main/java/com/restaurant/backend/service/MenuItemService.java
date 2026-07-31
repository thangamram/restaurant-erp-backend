package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.MenuItemDto;
import com.restaurant.backend.entity.Category;
import com.restaurant.backend.entity.MenuItem;
import com.restaurant.backend.enums.DietaryType;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.CategoryRepository;
import com.restaurant.backend.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public MenuItemDto.MenuItemResponse createMenuItem(MenuItemDto.MenuItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        MenuItem menuItem = MenuItem.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .gstPercentage(request.getGstPercentage())
                .prepTimeMinutes(request.getPrepTimeMinutes() != null ? request.getPrepTimeMinutes() : 15)
                .dietaryType(request.getDietaryType() != null ? request.getDietaryType() : DietaryType.VEG)
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .imageUrl(request.getImageUrl())
                .deleted(false)
                .build();

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Transactional
    public MenuItemDto.MenuItemResponse updateMenuItem(Long id, MenuItemDto.MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        menuItem.setCategory(category);
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        if (request.getGstPercentage() != null) menuItem.setGstPercentage(request.getGstPercentage());
        if (request.getPrepTimeMinutes() != null) menuItem.setPrepTimeMinutes(request.getPrepTimeMinutes());
        if (request.getDietaryType() != null) menuItem.setDietaryType(request.getDietaryType());
        if (request.getAvailable() != null) menuItem.setAvailable(request.getAvailable());
        if (request.getImageUrl() != null) menuItem.setImageUrl(request.getImageUrl());

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Transactional(readOnly = true)
    public MenuItemDto.MenuItemResponse getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        return mapToResponse(menuItem);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemDto.MenuItemResponse> searchMenuItems(Long categoryId, DietaryType dietaryType, Boolean available, String search, Pageable pageable) {
        return menuItemRepository.searchMenuItems(categoryId, dietaryType, available, search, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public MenuItemDto.MenuItemResponse toggleAvailability(Long id, boolean available) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        menuItem.setAvailable(available);
        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        menuItem.setDeleted(true); // Soft delete
        menuItemRepository.save(menuItem);
    }

    private MenuItemDto.MenuItemResponse mapToResponse(MenuItem item) {
        MenuItemDto.MenuItemResponse res = new MenuItemDto.MenuItemResponse();
        res.setId(item.getId());
        res.setCategoryId(item.getCategory().getId());
        res.setCategoryName(item.getCategory().getName());
        res.setName(item.getName());
        res.setDescription(item.getDescription());
        res.setPrice(item.getPrice());
        res.setGstPercentage(item.getGstPercentage());
        res.setPrepTimeMinutes(item.getPrepTimeMinutes());
        res.setDietaryType(item.getDietaryType());
        res.setAvailable(item.isAvailable());
        res.setImageUrl(item.getImageUrl());
        return res;
    }
}
