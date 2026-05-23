package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.entity.ShowtimeSeat;
import com.tranvanluan.backend.entity.ShowtimeSeat.SeatStatus;
import com.tranvanluan.backend.repository.ShowtimeSeatRepository;
import com.tranvanluan.backend.service.ShowtimeSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeSeatServiceImpl implements ShowtimeSeatService {

    private final ShowtimeSeatRepository showtimeSeatRepository;

    @Override
    public List<ShowtimeSeat> getByShowtimeId(Long showtimeId) {
        return showtimeSeatRepository.findByShowtimeIdWithSeat(showtimeId);
    }

    @Override
    public List<Long> getBookedSeatIds(Long showtimeId) {
        return showtimeSeatRepository.findByShowtimeIdWithSeat(showtimeId) // ✅ đổi ở đây
                .stream()
                .filter(ss -> ss.getStatus() == SeatStatus.BOOKED
                        || (ss.getStatus() == SeatStatus.HOLD && !ss.isHoldExpired()))
                .map(ss -> ss.getSeat().getId())
                .toList();
    }
}