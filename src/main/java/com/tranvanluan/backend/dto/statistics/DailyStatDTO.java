package com.tranvanluan.backend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyStatDTO {
    private LocalDate showDate;
    private int totalTickets;
    private double totalAmount;
}
