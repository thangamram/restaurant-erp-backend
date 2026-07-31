package com.restaurant.backend.repository;

import com.restaurant.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByMobileNumber(String mobileNumber);

    @Query("SELECT u FROM User u WHERE u.username = :loginVal OR u.email = :loginVal OR u.mobileNumber = :loginVal")
    Optional<User> findByUsernameOrEmailOrMobileNumber(@Param("loginVal") String loginVal);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByMobileNumber(String mobileNumber);
}