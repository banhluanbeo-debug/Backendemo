package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.entity.Room;
import com.tranvanluan.backend.entity.Seat;
import com.tranvanluan.backend.entity.Showtime;
import com.tranvanluan.backend.entity.ShowtimeSeat;
import com.tranvanluan.backend.repository.RoomRepository;
import com.tranvanluan.backend.repository.SeatRepository;
import com.tranvanluan.backend.repository.ShowtimeRepository;
import com.tranvanluan.backend.repository.ShowtimeSeatRepository;
import com.tranvanluan.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final com.tranvanluan.backend.repository.OrderDetailRepository orderDetailRepository;

    @Override
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    @Override
    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id " + id));
    }

    @Override
    public Room create(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Room update(Long id, Room room) {
        Room existing = getById(id);
        existing.setName(room.getName());
        existing.setSeatsCount(room.getSeatsCount());
        existing.setCinema(room.getCinema());
        return roomRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new NoSuchElementException("Room not found with id " + id);
        }
        boolean hasOrders = orderDetailRepository.existsByShowtimeSeat_Seat_RoomId(id);
        if (hasOrders) {
            throw new IllegalArgumentException("Không thể xóa phòng chiếu vì đã có khách hàng đặt vé trong phòng này!");
        }
        roomRepository.deleteById(id);
    }

    @Override
    public int syncSeatsWithShowtimes(Long roomId) {

        List<Seat> seats = seatRepository.findByRoomId(roomId);

        List<Showtime> showtimes = showtimeRepository.findByRoomId(roomId);

        int count = 0;

        for (Showtime showtime : showtimes) {
            for (Seat seat : seats) {

                boolean exists = showtimeSeatRepository
                        .existsByShowtimeIdAndSeatId(
                                showtime.getId(),
                                seat.getId());

                if (!exists) {

                    ShowtimeSeat ss = ShowtimeSeat.builder()
                            .showtime(showtime)
                            .seat(seat)
                            .status(
                                    ShowtimeSeat.SeatStatus.AVAILABLE)
                            .build();

                    showtimeSeatRepository.save(ss);

                    count++;
                }
            }
        }

        return count;
    }
}
