package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.service.CinemaService;
import com.tranvanluan.backend.entity.Cinema;
import com.tranvanluan.backend.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CinemaServiceImpl implements CinemaService {

    private final CinemaRepository cinemaRepository;
    private final com.tranvanluan.backend.repository.OrderDetailRepository orderDetailRepository;

    @Override
    public List<Cinema> getAll() {
        return cinemaRepository.findAll();
    }

    @Override
    public Cinema getById(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cinema not found with id " + id));
    }

    @Override
    public Cinema create(Cinema cinema) {
        return cinemaRepository.save(cinema);
    }

    @Override
    public Cinema update(Long id, Cinema cinema) {
        Cinema existing = getById(id);
        existing.setName(cinema.getName());
        existing.setAddress(cinema.getAddress());
        return cinemaRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!cinemaRepository.existsById(id)) {
            throw new NoSuchElementException("Cinema not found with id " + id);
        }
        boolean hasOrders = orderDetailRepository.existsByShowtimeSeat_Seat_Room_CinemaId(id);
        if (hasOrders) {
            throw new IllegalArgumentException("Không thể xóa rạp chiếu vì đã có khách hàng đặt vé xem phim tại rạp này!");
        }
        cinemaRepository.deleteById(id);
    }
}
