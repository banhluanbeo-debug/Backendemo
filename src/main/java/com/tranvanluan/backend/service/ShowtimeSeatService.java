package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.ShowtimeSeat;
import java.util.List;

public interface ShowtimeSeatService {
    List<ShowtimeSeat> getByShowtimeId(Long showtimeId);

    List<Long> getBookedSeatIds(Long showtimeId);
}