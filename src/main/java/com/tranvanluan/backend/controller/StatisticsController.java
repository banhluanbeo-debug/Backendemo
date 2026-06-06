package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.dto.statistics.DailyStatDTO;
import com.tranvanluan.backend.dto.statistics.MovieStatDTO;
import com.tranvanluan.backend.dto.statistics.ShowtimeStatDTO;
import com.tranvanluan.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly")
    public List<MovieStatDTO> getMonthlyStats(
            @RequestParam int month,
            @RequestParam int year) {
        return statisticsService.getMonthlyStatsByMovie(month, year);
    }

    @GetMapping("/movie/{movieId}/daily")
    public List<DailyStatDTO> getDailyStats(
            @PathVariable Long movieId,
            @RequestParam int month,
            @RequestParam int year) {
        return statisticsService.getDailyStatsForMovie(movieId, month, year);
    }

    @GetMapping("/movie/{movieId}/showtimes")
    public List<ShowtimeStatDTO> getShowtimeStats(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return statisticsService.getShowtimeStatsForMovieAndDate(movieId, date);
    }

    @GetMapping("/user/{userId}/history")
    public List<com.tranvanluan.backend.dto.statistics.UserHistoryDTO> getUserHistory(@PathVariable Long userId) {
        return statisticsService.getUserHistory(userId);
    }
}
