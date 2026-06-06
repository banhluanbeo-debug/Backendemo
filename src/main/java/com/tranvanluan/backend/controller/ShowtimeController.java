package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.dto.CreateShowtimeRequestDTO;
import com.tranvanluan.backend.dto.ShowtimeDTO;
import com.tranvanluan.backend.entity.Movie;
import com.tranvanluan.backend.entity.Room;
import com.tranvanluan.backend.entity.Showtime;
import com.tranvanluan.backend.repository.MovieRepository;
import com.tranvanluan.backend.repository.RoomRepository;
import com.tranvanluan.backend.service.ShowtimeService;
import com.tranvanluan.backend.service.mapper.ShowtimeMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final ShowtimeMapper showtimeMapper;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @GetMapping
    public List<ShowtimeDTO> getAllShowtimes() {
        return showtimeService.getAll().stream()
                .map(showtimeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ShowtimeDTO getShowtimeById(@PathVariable Long id) {
        return showtimeMapper.toDTO(showtimeService.getById(id));
    }

    @PostMapping
    public Showtime createShowtime(@RequestBody CreateShowtimeRequestDTO request) { // đổi
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .price(request.getPrice())
                .build();

        return showtimeService.create(showtime);
    }

    @PostMapping("/bulk")
    public java.util.Map<String, Integer> createBulkShowtimes(@RequestBody com.tranvanluan.backend.dto.BulkCreateShowtimeRequestDTO request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return showtimeService.createBulk(request, movie, room);
    }

    @PostMapping("/bulk-delete")
    public java.util.Map<String, Integer> deleteBulkShowtimes(@RequestBody List<Long> ids) {
        return showtimeService.deleteBulk(ids);
    }

    @PutMapping("/{id}")
    public Showtime updateShowtime(@PathVariable Long id, @RequestBody CreateShowtimeRequestDTO request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .price(request.getPrice())
                .build();
        return showtimeService.update(id, showtime);
    }

    @DeleteMapping("/{id}")
    public void deleteShowtime(@PathVariable Long id) {
        showtimeService.delete(id);
    }
}
