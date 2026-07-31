package com.restaurant.backend.service;

import com.restaurant.backend.dto.request.RestaurantSettingDto;
import com.restaurant.backend.entity.RestaurantSetting;
import com.restaurant.backend.exception.ResourceNotFoundException;
import com.restaurant.backend.repository.RestaurantSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {

    private final RestaurantSettingRepository settingRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<RestaurantSettingDto.SettingResponse> getAllSettings() {
        return settingRepository.findAll().stream()
                .map(this::mapToSettingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantSettingDto.SettingResponse getSettingByKey(String key) {
        return settingRepository.findBySettingKey(key)
                .map(this::mapToSettingResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Setting", "key", key));
    }

    @Transactional
    public RestaurantSettingDto.SettingResponse updateSetting(String key, RestaurantSettingDto.SettingRequest request, String performedBy, String ipAddress) {
        RestaurantSetting setting = settingRepository.findBySettingKey(key)
                .orElseGet(() -> RestaurantSetting.builder().settingKey(key).build());

        setting.setSettingValue(request.getSettingValue());
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }

        RestaurantSetting saved = settingRepository.save(setting);
        
        auditService.logAction(performedBy, "ADMIN", "SETTING_UPDATED", "SYSTEM",
                "Updated setting '" + key + "'", ipAddress);

        return mapToSettingResponse(saved);
    }

    private RestaurantSettingDto.SettingResponse mapToSettingResponse(RestaurantSetting setting) {
        RestaurantSettingDto.SettingResponse res = new RestaurantSettingDto.SettingResponse();
        res.setId(setting.getId());
        res.setSettingKey(setting.getSettingKey());
        res.setSettingValue(setting.getSettingValue());
        res.setDescription(setting.getDescription());
        return res;
    }
}
