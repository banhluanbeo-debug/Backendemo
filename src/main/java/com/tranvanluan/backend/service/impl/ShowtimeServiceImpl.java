package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.entity.Showtime;
import com.tranvanluan.backend.entity.ShowtimeSeat;
import com.tranvanluan.backend.entity.ShowtimeSeat.SeatStatus;
import com.tranvanluan.backend.entity.Seat;
import com.tranvanluan.backend.entity.Room;
import com.tranvanluan.backend.repository.ShowtimeRepository;
import com.tranvanluan.backend.repository.ShowtimeSeatRepository;
import com.tranvanluan.backend.repository.SeatRepository;
import com.tranvanluan.backend.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final SeatRepository seatRepository;
    private final com.tranvanluan.backend.repository.RoomRepository roomRepository;
    private final com.tranvanluan.backend.repository.OrderDetailRepository orderDetailRepository;

    @org.springframework.context.event.EventListener
    @Transactional
    public void onApplicationReady(org.springframework.context.event.ContextRefreshedEvent event) {
        try {
            List<Room> rooms = roomRepository.findAll();
            for (Room room : rooms) {
                List<Seat> roomSeats = seatRepository.findByRoomId(room.getId());
                if (roomSeats.isEmpty()) {
                    log.info("Room '{}' (ID {}) has 0 seats. Generating default layout (A1-A6, B1-B6)...", room.getName(), room.getId());
                    java.util.List<Seat> newSeats = new java.util.ArrayList<>();
                    String[] rows = {"A", "B"};
                    for (String row : rows) {
                        for (int i = 1; i <= 6; i++) {
                            newSeats.add(Seat.builder()
                                    .code(row + i)
                                    .type("REGULAR")
                                    .status(true)
                                    .room(room)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build());
                        }
                    }
                    seatRepository.saveAll(newSeats);
                }
            }

            List<Showtime> showtimes = showtimeRepository.findAll();
            int count = 0;
            for (Showtime st : showtimes) {
                List<ShowtimeSeat> existing = showtimeSeatRepository.findByShowtimeId(st.getId());
                if (existing.isEmpty()) {
                    List<Seat> seats = seatRepository.findByRoomId(st.getRoom().getId());
                    if (seats.isEmpty()) {
                        log.warn("Room ID {} for showtime ID {} has no seats defined in seats table!", st.getRoom().getId(), st.getId());
                        continue;
                    }
                    List<ShowtimeSeat> showtimeSeats = seats.stream()
                            .map(seat -> ShowtimeSeat.builder()
                                    .showtime(st)
                                    .seat(seat)
                                    .status(SeatStatus.AVAILABLE)
                                    .holdUntil(null)
                                    .user(null)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build())
                            .toList();
                    showtimeSeatRepository.saveAll(showtimeSeats);
                    count += showtimeSeats.size();
                }
            }
            if (count > 0) {
                log.info("Successfully auto-generated {} missing seats for legacy showtimes on startup.", count);
            }
        } catch (Exception e) {
            log.error("Failed to generate missing seats on startup", e);
        }
    }

    @Override
    public List<Showtime> getAll() {
        return showtimeRepository.findAll();
    }

    @Override
    public Showtime getById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu với id: " + id));
    }

    @Override
    @Transactional
    public Showtime create(Showtime showtime) {
        validateShowtime(showtime);

        Showtime saved = showtimeRepository.save(showtime);

        List<Seat> seats = seatRepository.findByRoomId(saved.getRoom().getId());

        List<ShowtimeSeat> showtimeSeats = seats.stream()
                .map(seat -> ShowtimeSeat.builder()
                        .showtime(saved)
                        .seat(seat)
                        .status(SeatStatus.AVAILABLE)
                        .holdUntil(null)
                        .user(null)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .toList();

        showtimeSeatRepository.saveAll(showtimeSeats);

        return saved;
    }

    @Override
    @Transactional
    public java.util.Map<String, Integer> createBulk(com.tranvanluan.backend.dto.BulkCreateShowtimeRequestDTO request, com.tranvanluan.backend.entity.Movie movie, com.tranvanluan.backend.entity.Room room) {
        int created = 0;
        int skipped = 0;

        LocalDate loopStart = request.getStartDate();
        LocalDate loopEnd = request.getEndDate();

        if (movie.getReleaseDate() != null && loopStart.isBefore(movie.getReleaseDate())) {
            loopStart = movie.getReleaseDate();
        }
        if (movie.getEndDate() != null && loopEnd.isAfter(movie.getEndDate())) {
            loopEnd = movie.getEndDate();
        }

        if (loopStart.isAfter(loopEnd)) {
            throw new IllegalArgumentException("Khoảng thời gian tạo suất chiếu không hợp lệ hoặc nằm ngoài lịch chiếu của phim!");
        }

        LocalDate currentDate = loopStart;
        while (!currentDate.isAfter(loopEnd)) {
            for (java.time.LocalTime time : request.getTimes()) {
                Showtime showtime = Showtime.builder()
                        .movie(movie)
                        .room(room)
                        .showDate(currentDate)
                        .showTime(time)
                        .price(request.getPrice())
                        .build();

                try {
                    validateShowtime(showtime);

                    Showtime saved = showtimeRepository.save(showtime);
                    List<Seat> seats = seatRepository.findByRoomId(room.getId());
                    List<ShowtimeSeat> showtimeSeats = seats.stream()
                            .map(seat -> ShowtimeSeat.builder()
                                    .showtime(saved)
                                    .seat(seat)
                                    .status(SeatStatus.AVAILABLE)
                                    .holdUntil(null)
                                    .user(null)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build())
                            .toList();
                    showtimeSeatRepository.saveAll(showtimeSeats);
                    created++;
                } catch (IllegalArgumentException e) {
                    skipped++;
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("created", created);
        result.put("skipped", skipped);
        return result;
    }

    @Override
    @Transactional
    public Showtime update(Long id, Showtime showtime) {
        Showtime existing = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu với id: " + id));

        showtime.setId(id);     
        validateShowtime(showtime);

        boolean roomChanged = !existing.getRoom().getId().equals(showtime.getRoom().getId());
        boolean timeChanged = !existing.getShowDate().equals(showtime.getShowDate()) || !existing.getShowTime().equals(showtime.getShowTime());
        boolean movieChanged = !existing.getMovie().getId().equals(showtime.getMovie().getId());

        if (roomChanged || timeChanged || movieChanged) {
            boolean hasBookedOrHeld = showtimeSeatRepository.existsByShowtimeIdAndStatus(id, SeatStatus.BOOKED)
                    || showtimeSeatRepository.existsByShowtimeIdAndStatus(id, SeatStatus.HOLD);
            if (hasBookedOrHeld) {
                throw new IllegalArgumentException("Không thể thay đổi phòng, phim, hoặc thời gian cho suất chiếu đã có người chọn hoặc đặt vé!");
            }
        }

        existing.setShowDate(showtime.getShowDate());
        existing.setShowTime(showtime.getShowTime());
        existing.setPrice(showtime.getPrice());
        existing.setMovie(showtime.getMovie());
        existing.setRoom(showtime.getRoom());
        existing.setUpdatedAt(LocalDateTime.now());

        Showtime saved = showtimeRepository.save(existing);

        List<ShowtimeSeat> existingSeats = showtimeSeatRepository.findByShowtimeId(id);
        if (existingSeats.isEmpty() || roomChanged) {
            if (roomChanged && !existingSeats.isEmpty()) {
                showtimeSeatRepository.deleteAll(existingSeats);
            }

            List<Seat> seats = seatRepository.findByRoomId(saved.getRoom().getId());
            List<ShowtimeSeat> showtimeSeats = seats.stream()
                    .map(seat -> ShowtimeSeat.builder()
                            .showtime(saved)
                            .seat(seat)
                            .status(SeatStatus.AVAILABLE)
                            .holdUntil(null)
                            .user(null)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .toList();
            showtimeSeatRepository.saveAll(showtimeSeats);
        }

        return saved;
    }

    @Override
    public void delete(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy suất chiếu với id: " + id);
        }
        
        boolean hasBookedSeats = showtimeSeatRepository.existsByShowtimeIdAndStatus(id, SeatStatus.BOOKED);
        boolean hasOrders = orderDetailRepository.existsByShowtimeSeat_ShowtimeId(id);
        if (hasBookedSeats || hasOrders) {
            throw new IllegalArgumentException("Không thể xóa suất chiếu đã có người đặt vé hoặc đang được giao dịch!");
        }
        
        showtimeRepository.deleteById(id);
    }

    @Override
    public java.util.Map<String, Integer> deleteBulk(List<Long> ids) {
        int deleted = 0;
        int failed = 0;
        for (Long id : ids) {
            try {
                delete(id);
                deleted++;
            } catch (Exception e) {
                failed++;
            }
        }
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("deleted", deleted);
        result.put("failed", failed);
        return result;
    }

    @Override
    public List<Showtime> getUpcomingShowtimes() {
        LocalDate today = LocalDate.now();
        return showtimeRepository.findByShowDateAfter(today);
    }

    private void validateShowtime(Showtime showtime) {
        LocalDateTime showtimeStart = LocalDateTime.of(showtime.getShowDate(), showtime.getShowTime());

        if (showtimeStart.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Suất chiếu phải ở tương lai!");
        }

        com.tranvanluan.backend.entity.Movie movie = showtime.getMovie();
        if (movie.getReleaseDate() != null && showtime.getShowDate().isBefore(movie.getReleaseDate())) {
            throw new IllegalArgumentException("Phim chưa công chiếu vào ngày này!");
        }
        if (movie.getEndDate() != null && showtime.getShowDate().isAfter(movie.getEndDate())) {
            throw new IllegalArgumentException("Phim đã ngừng chiếu vào ngày này!");
        }

        int duration = movie.getDuration() != null ? movie.getDuration() : 120;
        LocalDateTime showtimeEnd = showtimeStart.plusMinutes(duration);

        List<Showtime> existingShowtimes = showtimeRepository.findByRoom_IdAndShowDate(showtime.getRoom().getId(), showtime.getShowDate());

        for (Showtime existing : existingShowtimes) {
            if (showtime.getId() != null && existing.getId().equals(showtime.getId())) {
                continue;
            }
            LocalDateTime existingStart = LocalDateTime.of(existing.getShowDate(), existing.getShowTime());
            int existingDuration = existing.getMovie().getDuration() != null ? existing.getMovie().getDuration() : 120;
            LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);

            if (showtimeStart.isBefore(existingEnd) && showtimeEnd.isAfter(existingStart)) {
                throw new IllegalArgumentException(String.format("Phòng này đã có suất chiếu phim '%s' (từ %s đến %s)!", existing.getMovie().getTitle(), existingStart.toLocalTime(), existingEnd.toLocalTime()));
            }
        }
    }
}