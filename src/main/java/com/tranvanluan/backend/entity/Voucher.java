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
    private String code; // vd: FOOD30-X82KD

    private Double discountAmount; // số tiền giảm (vd: 30000, 50000)

    private String type; // FOOD30 | FOOD50

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status = VoucherStatus.UNUSED;

    private Long userId; // gắn với user cụ thể

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime usedAt;

    public enum VoucherStatus {
        UNUSED, USED
    }
}
