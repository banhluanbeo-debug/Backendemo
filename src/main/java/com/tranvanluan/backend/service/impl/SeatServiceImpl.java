    package com.tranvanluan.backend.service.impl;

    import com.tranvanluan.backend.entity.Seat;
    import com.tranvanluan.backend.entity.Showtime;
    import com.tranvanluan.backend.entity.ShowtimeSeat;
    import com.tranvanluan.backend.entity.ShowtimeSeat.SeatStatus;
    import com.tranvanluan.backend.repository.SeatRepository;
    import com.tranvanluan.backend.repository.ShowtimeRepository;
    import com.tranvanluan.backend.repository.ShowtimeSeatRepository;
    import com.tranvanluan.backend.service.SeatService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.NoSuchElementException;

    @Service
    @RequiredArgsConstructor
    public class SeatServiceImpl implements SeatService {

        private final SeatRepository seatRepository;
        private final ShowtimeSeatRepository showtimeSeatRepository;
        private final ShowtimeRepository showtimeRepository;
        private final com.tranvanluan.backend.repository.OrderDetailRepository orderDetailRepository;

        @Override
        public List<Seat> getAll() {
            return seatRepository.findAll();
        }

        @Override
        public Seat getById(Long id) {
            return seatRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Seat not found with id " + id));
        }

        @Override
        @Transactional
        public Seat create(Seat seat) {
            Seat saved = seatRepository.save(seat);

            // Tự động tạo ShowtimeSeat cho tất cả suất chiếu hiện có trong phòng
            if (saved.getRoom() != null && saved.getRoom().getId() != null) {
                List<Showtime> showtimes = showtimeRepository.findByRoomId(saved.getRoom().getId());
                List<ShowtimeSeat> showtimeSeats = showtimes.stream()
                        .map(st -> ShowtimeSeat.builder()
                                .showtime(st)
                                .seat(saved)
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
        public Seat update(Long id, Seat seat) {
            Seat existing = getById(id);
            existing.setCode(seat.getCode());
            existing.setType(seat.getType());
            existing.setStatus(seat.getStatus());
            existing.setRoom(seat.getRoom());
            return seatRepository.save(existing);
        }

        @Override
        @Transactional
        public void delete(Long id) {
            if (!seatRepository.existsById(id)) {
                throw new NoSuchElementException("Seat not found with id " + id);
            }

            boolean hasBooked = showtimeSeatRepository.existsBySeatIdAndStatus(id, SeatStatus.BOOKED);
            boolean hasHold = showtimeSeatRepository.existsBySeatIdAndStatus(id, SeatStatus.HOLD);
            boolean hasOrders = orderDetailRepository.existsByShowtimeSeat_SeatId(id);
            
            if (hasBooked || hasHold || hasOrders) {
                throw new IllegalArgumentException("Không thể xoá ghế vì đã có người đặt vé hoặc đang giữ chỗ ở các suất chiếu!");
            }

            showtimeSeatRepository.deleteBySeatId(id);
            seatRepository.deleteById(id);
        }
    }
