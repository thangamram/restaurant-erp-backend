package com.restaurant.backend.repository;

import com.restaurant.backend.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndActiveTrue(String code);
    Optional<Coupon> findByCode(String code);
    Boolean existsByCode(String code);
}
