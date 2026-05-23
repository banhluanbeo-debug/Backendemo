package com.tranvanluan.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tranvanluan.backend.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
