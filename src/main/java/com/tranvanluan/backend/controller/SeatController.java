package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.entity.Seat;
import com.tranvanluan.backend.entity.ShowtimeSeat;
import com.tranvanluan.backend.service.SeatService;
import com.tranvanluan.backend.service.ShowtimeSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final ShowtimeSeatService showtimeSeatService;

    @GetMapping
    public List<Seat> getAllSeats() {
        return seatService.getAll();
    }

    @GetMapping("/{id}")
    public Seat getSeatById(@PathVariable Long id) {
        return seatService.getById(id);
    }

    @PostMapping
    public Seat createSeat(@RequestBody Seat seat) {
        return seatService.create(seat);
    }

    @PutMapping("/{id}")
    public Seat updateSeat(@PathVariable Long id, @RequestBody Seat seat) {
        return seatService.update(id, seat);
    }

    @DeleteMapping("/{id}")
    public void deleteSeat(@PathVariable Long id) {
        seatService.delete(id);
    }

   
    @GetMapping("/showtime-seats")
    public List<ShowtimeSeat> getShowtimeSeats(@RequestParam Long showtimeId) {
        return showtimeSeatService.getByShowtimeId(showtimeId);
    }

    // Trả về tất cả ghế không thể chọn (BOOKED + HOLD còn hiệu lực)
    // Frontend dùng endpoint này để render ghế bị khóa
    @GetMapping("/unavailable")
    public List<Long> getUnavailableSeatIds(@RequestParam Long showtimeId) {
        return showtimeSeatService.getBookedSeatIds(showtimeId);
    }
}