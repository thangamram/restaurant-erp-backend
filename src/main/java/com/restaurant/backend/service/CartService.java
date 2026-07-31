package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.CartDto;
import com.restaurant.backend.entity.*;
import com.restaurant.backend.exception.BadRequestException;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository tableRepository;

    @Transactional
    public CartDto.CartResponse getOrCreateCart(Long customerId, Long tableId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .customer(customer)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        if (tableId != null) {
            RestaurantTable table = tableRepository.findById(tableId)
                    .orElseThrow(() -> new ResourceNotFoundException("RestaurantTable", "id", tableId));
            cart.setTable(table);
            cartRepository.save(cart);
        }

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartDto.CartResponse addItemToCart(Long customerId, CartDto.CartItemRequest request) {
        CartDto.CartResponse cartResponse = getOrCreateCart(customerId, null);
        Cart cart = cartRepository.findById(cartResponse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartResponse.getId()));

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()));

        if (!menuItem.isAvailable() || menuItem.isDeleted()) {
            throw new BadRequestException("Menu item '" + menuItem.getName() + "' is currently unavailable");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItem.getId());
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (request.getSpecialInstructions() != null) {
                item.setSpecialInstructions(request.getSpecialInstructions());
            }
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .specialInstructions(request.getSpecialInstructions())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return mapToCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartDto.CartResponse updateCartItemQuantity(Long customerId, Long cartItemId, int quantity) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for customer", "id", customerId));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to user's cart");
        }

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return mapToCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartDto.CartResponse removeCartItem(Long customerId, Long cartItemId) {
        return updateCartItemQuantity(customerId, cartItemId, 0);
    }

    @Transactional
    public void clearCart(Long customerId) {
        cartRepository.findByCustomerId(customerId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private CartDto.CartResponse mapToCartResponse(Cart cart) {
        CartDto.CartResponse res = new CartDto.CartResponse();
        res.setId(cart.getId());
        res.setCustomerId(cart.getCustomer().getId());
        if (cart.getTable() != null) {
            res.setTableId(cart.getTable().getId());
            res.setTableNumber(cart.getTable().getTableNumber());
        }

        BigDecimal total = BigDecimal.ZERO;
        var itemResponses = new ArrayList<CartDto.CartItemResponse>();

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(subtotal);

                CartDto.CartItemResponse itemRes = new CartDto.CartItemResponse();
                itemRes.setId(item.getId());
                itemRes.setMenuItemId(item.getMenuItem().getId());
                itemRes.setMenuItemName(item.getMenuItem().getName());
                itemRes.setMenuItemImage(item.getMenuItem().getImageUrl());
                itemRes.setQuantity(item.getQuantity());
                itemRes.setUnitPrice(item.getUnitPrice());
                itemRes.setSubtotal(subtotal);
                itemRes.setSpecialInstructions(item.getSpecialInstructions());
                itemResponses.add(itemRes);
            }
        }

        res.setItems(itemResponses);
        res.setCartTotal(total);
        return res;
    }
}
