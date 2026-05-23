package com.tranvanluan.backend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHistoryDTO {
    private String movieTitle;
    private String posterUrl;
    private LocalDate showDate;
    private LocalTime showTime;
    private int ticketCount;
    private double totalAmount;
    private String status;
}
