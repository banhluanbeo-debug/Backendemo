package com.tranvanluan.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.tranvanluan.backend.entity.AuthProvider;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phone;
    
    private String avatar;
    
    private java.time.LocalDate dob;

    private String password; // nullable nếu login Google

    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // LOCAL, GOOGLE

    private String providerId; // id từ Google

    private String role = "USER";

    private Boolean status = true;

    private Integer rewardLevel = 0; // 0 = chưa nhận gì, 5 = đã nhận mốc 5, 10 = đã nhận mốc 10

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}