package com.tranvanluan.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequestDTO {
    private Long userId;
    private Long showtimeId;
    private List<Long> seatIds;        // danh sách ghế đã chọn
    private String paymentMethod;      // CASH | VNPAY

    // Food & Voucher
    private List<FoodItemDTO> foodItems; // nullable nếu không chọn đồ ăn
    private String voucherCode;          // nullable nếu không dùng voucher
    private Double foodTotal;            // tổng tiền đồ ăn (trước giảm)
    private Double discountAmount;       // số tiền voucher giảm

    @Data
    public static class FoodItemDTO {
        private String name;
        private Double price;
        private Integer quantity;
    }
}