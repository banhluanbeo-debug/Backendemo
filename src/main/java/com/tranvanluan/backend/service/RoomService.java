package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Room;
import java.util.List;

public interface RoomService {
    List<Room> getAll();

    Room getById(Long id);

    Room create(Room room);

    Room update(Long id, Room room);

    void delete(Long id);

    int syncSeatsWithShowtimes(Long roomId);

}
