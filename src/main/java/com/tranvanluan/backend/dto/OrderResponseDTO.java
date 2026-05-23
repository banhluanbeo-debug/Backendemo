package com.tranvanluan.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long id;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private UserDTO user;
    private List<OrderDetailDTO> orderDetails;
    private LocalDateTime createdAt;

    // Thêm cho history view
    private String seatCodes;
    private String movieTitle;
    private Double foodTotal;
    private Double discountAmount;
    private String voucherCode;
}
