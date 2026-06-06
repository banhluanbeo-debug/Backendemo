package com.tranvanluan.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private Double discountAmount;

    private String type; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status = VoucherStatus.UNUSED;

    private Long userId; 

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime usedAt;

    public enum VoucherStatus {
        UNUSED, USED
    }
}
