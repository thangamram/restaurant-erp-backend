package com.restaurant.backend.controller;

import com.restaurant.backend.dto.request.RestaurantSettingDto;
import com.restaurant.backend.dto.response.ApiResponse;
import com.restaurant.backend.security.UserPrincipal;
import com.restaurant.backend.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RestaurantSettingDto.SettingResponse>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success("Settings fetched successfully", settingsService.getAllSettings()));
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingDto.SettingResponse>> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success("Setting fetched successfully", settingsService.getSettingByKey(key)));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingDto.SettingResponse>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody RestaurantSettingDto.SettingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", 
                settingsService.updateSetting(key, request, currentUser.getUsername(), ipAddress)));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
