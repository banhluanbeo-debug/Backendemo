package com.tranvanluan.backend.repository;

import com.tranvanluan.backend.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // Kiểm tra xem đã có suất chiếu trùng trong cùng phòng chưa
    boolean existsByRoom_IdAndShowDateAndShowTime(Long roomId, LocalDate showDate, LocalTime showTime);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId AND s.showDate = :showDate")
    List<Showtime> findByRoom_IdAndShowDate(@org.springframework.data.repository.query.Param("roomId") Long roomId, @org.springframework.data.repository.query.Param("showDate") LocalDate showDate);

    List<Showtime> findByShowDateAfter(LocalDate date);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM Showtime s WHERE s.id = :showtimeId")
    void deleteByShowtimeId(@org.springframework.data.repository.query.Param("showtimeId") Long showtimeId);

    List<Showtime> findByRoomId(Long roomId);
}
