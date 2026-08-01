package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.OrderDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.PageResponse;
import com.restaurant.backend.enums.OrderStatus;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_WAITER', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody OrderDto.CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Order placed successfully", 
                orderService.placeOrder(currentUser.getId(), request, ipAddress)), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CASHIER') or @securityService.isOrderOwner(#id)")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", orderService.getOrderById(id)));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderResponse>>> getMyOrders(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orderService.getOrdersByCustomer(currentUser.getId())));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto.OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "placedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Page<OrderDto.OrderResponse> orderPage = orderService.getAllOrders(PageRequest.of(page, size, sort));

        PageResponse<OrderDto.OrderResponse> response = new PageResponse<>(
                orderPage.getContent(), orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages(), orderPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderResponse>>> getActiveOrders() {
        return ResponseEntity.ok(ApiResponse.success("Active orders fetched successfully", orderService.getActiveOrders()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orderService.getOrdersByStatus(status)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto.UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", 
                orderService.updateOrderStatus(id, request, currentUser.getUsername(), ipAddress)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER') or @securityService.isOrderOwner(#id)")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", 
                orderService.cancelOrder(id, currentUser.getUsername(), ipAddress)));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
