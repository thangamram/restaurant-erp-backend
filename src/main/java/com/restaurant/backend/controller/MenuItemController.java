package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.MenuItemDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.PageResponse;
import com.restaurant.backend.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MenuItemDto.MenuItemResponse>>> getAllMenuItems(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) com.restaurant.backend.enums.DietaryType dietaryType,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Page<MenuItemDto.MenuItemResponse> menuPage = menuItemService.searchMenuItems(
                categoryId, dietaryType, available, search, PageRequest.of(page, size, sort));

        PageResponse<MenuItemDto.MenuItemResponse> response = new PageResponse<>(
                menuPage.getContent(), menuPage.getNumber(), menuPage.getSize(),
                menuPage.getTotalElements(), menuPage.getTotalPages(), menuPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Menu items fetched successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemDto.MenuItemResponse>> getMenuItemById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Menu item fetched successfully", menuItemService.getMenuItemById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemDto.MenuItemResponse>> createMenuItem(@Valid @RequestBody MenuItemDto.MenuItemRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Menu item created successfully", menuItemService.createMenuItem(request)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemDto.MenuItemResponse>> updateMenuItem(
            @PathVariable Long id, @Valid @RequestBody MenuItemDto.MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", menuItemService.updateMenuItem(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully"));
    }
}
