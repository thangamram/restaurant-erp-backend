package com.restaurant.backend.repository;

import com.restaurant.backend.entity.RestaurantSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantSettingRepository extends JpaRepository<RestaurantSetting, Long> {
    Optional<RestaurantSetting> findBySettingKey(String settingKey);
}
