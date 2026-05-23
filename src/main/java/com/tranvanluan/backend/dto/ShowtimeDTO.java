package com.tranvanluan.backend.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeDTO {
    private Long id;
    private LocalDate showDate;
    private LocalTime showTime;
    private Double price;

    private Long movieId; // <- thêm
    private String movieTitle; // <- thêm
    private Long roomId; // <- thêm
    private String roomName; // <- thêm
}
