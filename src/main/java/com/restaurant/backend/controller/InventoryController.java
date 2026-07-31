package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.InventoryDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/ingredients")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<List<InventoryDto.IngredientResponse>>> getAllIngredients() {
        return ResponseEntity.ok(ApiResponse.success("Ingredients fetched successfully", inventoryService.getAllIngredients()));
    }

    @GetMapping("/ingredients/low-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<List<InventoryDto.IngredientResponse>>> getLowStockIngredients() {
        return ResponseEntity.ok(ApiResponse.success("Low stock ingredients fetched", inventoryService.getLowStockIngredients()));
    }

    @GetMapping("/ingredients/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<InventoryDto.IngredientResponse>> getIngredientById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ingredient fetched successfully", inventoryService.getIngredientById(id)));
    }

    @PostMapping("/ingredients")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDto.IngredientResponse>> createIngredient(
            @Valid @RequestBody InventoryDto.IngredientRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Ingredient created successfully", 
                inventoryService.createIngredient(request, currentUser.getUsername(), ipAddress)), HttpStatus.CREATED);
    }

    @PutMapping("/ingredients/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDto.IngredientResponse>> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDto.IngredientRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Ingredient updated successfully", 
                inventoryService.updateIngredient(id, request, currentUser.getUsername(), ipAddress)));
    }

    @PostMapping("/stock/adjust")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<InventoryDto.IngredientResponse>> adjustStock(
            @Valid @RequestBody InventoryDto.StockAdjustmentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", 
                inventoryService.adjustStock(request, currentUser.getUsername(), ipAddress)));
    }

    @PostMapping("/recipes")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDto.RecipeResponse>> addRecipe(
            @Valid @RequestBody InventoryDto.RecipeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Recipe added successfully", 
                inventoryService.addRecipe(request, currentUser.getUsername(), ipAddress)), HttpStatus.CREATED);
    }

    @GetMapping("/recipes/menu-item/{menuItemId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ApiResponse<List<InventoryDto.RecipeResponse>>> getRecipesByMenuItemId(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(ApiResponse.success("Recipes fetched successfully", inventoryService.getRecipesByMenuItemId(menuItemId)));
    }

    @DeleteMapping("/recipes/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        inventoryService.deleteRecipe(id, currentUser.getUsername(), ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Recipe deleted successfully"));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryDto.InventoryLogResponse>>> getInventoryLogs(
            @RequestParam(required = false) Long ingredientId) {
        return ResponseEntity.ok(ApiResponse.success("Logs fetched successfully", inventoryService.getInventoryLogs(ingredientId)));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
