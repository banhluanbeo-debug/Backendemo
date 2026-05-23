package com.tranvanluan.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tranvanluan.backend.entity.Seat;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRoomId(Long roomId);
}
