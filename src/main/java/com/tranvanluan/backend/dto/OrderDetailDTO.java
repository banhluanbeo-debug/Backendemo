package com.tranvanluan.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {

    private Long id;
    private Double price;
    private Integer quantity;
    private String seatCode;
    private String seatType; 

    private ShowtimeDTO showtime;
}
