package com.tranvanluan.backend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowtimeStatDTO {
    private LocalTime showTime;
    private String roomName;
    private int totalTickets;
    private double totalAmount;
}
