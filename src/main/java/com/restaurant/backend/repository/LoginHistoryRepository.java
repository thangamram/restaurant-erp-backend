package com.restaurant.backend.repository;

import com.restaurant.backend.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);
    Page<LoginHistory> findAllByOrderByLoginTimeDesc(Pageable pageable);
}
