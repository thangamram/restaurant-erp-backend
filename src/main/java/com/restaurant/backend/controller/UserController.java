package com.restaurant.backend.controller;

import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.dto.response.UserDto;
import com.restaurant.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER')")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<UserDto>>> getAllUsers(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(org.springframework.data.domain.PageRequest.of(page, size)), "Users fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CASHIER') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id), "User fetched successfully"));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.restaurant.backend.dto.request.UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request), "User updated successfully"));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}