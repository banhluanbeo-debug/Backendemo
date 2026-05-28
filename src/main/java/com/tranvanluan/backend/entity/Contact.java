package com.tranvanluan.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    // Trạng thái xử lý: NEW, IN_PROGRESS, RESOLVED
    @Column(nullable = false)
    @Builder.Default
    private String status = "NEW";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
