package com.tranvanluan.backend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieStatDTO {
    private Long movieId;
    private String movieTitle;
    private int totalTickets;
    private double totalAmount;
}
