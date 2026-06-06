package com.tranvanluan.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequestDTO {
    private Long userId;
    private Long showtimeId;
    private List<Long> seatIds;        
    private String paymentMethod;      

    // Food & Voucher
    private List<FoodItemDTO> foodItems; 
    private String voucherCode;          
    private Double foodTotal;            
    private Double discountAmount;       

    @Data
    public static class FoodItemDTO {
        private String name;
        private Double price;
        private Integer quantity;
    }
}