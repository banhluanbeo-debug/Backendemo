package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Seat;

import java.util.List;

public interface SeatService {
    List<Seat> getAll();

    Seat getById(Long id);

    Seat create(Seat seat);

    Seat update(Long id, Seat seat);

    void delete(Long id);
}
