package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.entity.Room;
import com.tranvanluan.backend.service.RoomService;
import com.tranvanluan.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SeatService seatService;

    @GetMapping
    public List<Room> getAll() {
        return roomService.getAll();
    }

    @GetMapping("/{id}")
    public Room getById(@PathVariable Long id) {
        return roomService.getById(id);
    }

    @PostMapping
    public Room create(@RequestBody Room room) {
        return roomService.create(room);
    }

    @PutMapping("/{id}")
    public Room update(@PathVariable Long id, @RequestBody Room room) {
        return roomService.update(id, room);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }

    @PostMapping("/{id}/sync-seats")
    public Map<String, Object> syncSeats(
            @PathVariable Long id) {

        int count = roomService.syncSeatsWithShowtimes(id);

        return Map.of(
                "message", "Đồng bộ thành công",
                "synced", count);
    }
}
