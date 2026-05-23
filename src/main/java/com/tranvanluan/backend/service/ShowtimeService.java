package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Showtime;

import java.util.List;

public interface ShowtimeService {
    List<Showtime> getAll();

    Showtime getById(Long id);

    Showtime create(Showtime showtime);

    java.util.Map<String, Integer> createBulk(com.tranvanluan.backend.dto.BulkCreateShowtimeRequestDTO request, com.tranvanluan.backend.entity.Movie movie, com.tranvanluan.backend.entity.Room room);

    Showtime update(Long id, Showtime showtime);

    void delete(Long id);

    java.util.Map<String, Integer> deleteBulk(List<Long> ids);
    
    List<Showtime> getUpcomingShowtimes();
}
