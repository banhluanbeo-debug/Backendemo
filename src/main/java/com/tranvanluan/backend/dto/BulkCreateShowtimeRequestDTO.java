package com.tranvanluan.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BulkCreateShowtimeRequestDTO {
    private Long movieId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<LocalTime> times;
    private Double price;
}
