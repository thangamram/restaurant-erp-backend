package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.CartDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> getCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully", cartService.getOrCreateCart(currentUser.getId(), null)));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> addItemToCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CartDto.CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cartService.addItemToCart(currentUser.getId(), request)));
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> updateCartItemQuantity(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated", cartService.updateCartItemQuantity(currentUser.getId(), cartItemId, quantity)));
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> removeCartItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(ApiResponse.success("Cart item removed", cartService.removeCartItem(currentUser.getId(), cartItemId)));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
}
