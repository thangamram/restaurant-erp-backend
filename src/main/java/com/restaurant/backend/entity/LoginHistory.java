package com.restaurant.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "login_time", nullable = false)
    @Builder.Default
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;
}
