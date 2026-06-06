package com.tranvanluan.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "order_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long originalOrderId;
    private Long userId;
    private Long showtimeId;

    private String movieTitle;
    private LocalDate showDate;
    private LocalTime showTime;
    private String roomName;

    private Double totalAmount;
    private String paymentMethod;
    private String status;

    @Column(length = 1000)
    private String seatCodes;

    private Double foodTotal = 0.0;
    private Double discountAmount = 0.0;
    private String voucherCode;

    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;
}