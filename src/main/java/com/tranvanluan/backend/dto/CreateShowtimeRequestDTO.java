// src/main/java/com/tranvanluan/backend/dto/CreateShowtimeRequestDTO.java
package com.tranvanluan.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateShowtimeRequestDTO {
    private Long movieId;
    private Long roomId;
    private LocalDate showDate;
    private LocalTime showTime;
    private Double price;
}