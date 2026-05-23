package com.tranvanluan.backend.repository;

import com.tranvanluan.backend.entity.ShowtimeSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {

        List<ShowtimeSeat> findByShowtimeId(Long showtimeId);

        boolean existsByShowtimeIdAndStatus(Long showtimeId, ShowtimeSeat.SeatStatus status);

        boolean existsBySeatIdAndStatus(Long seatId, ShowtimeSeat.SeatStatus status);

        // ✅ thêm mới: load kèm seat để tránh LazyInitializationException
        @Query("SELECT ss FROM ShowtimeSeat ss JOIN FETCH ss.seat WHERE ss.showtime.id = :showtimeId")
        List<ShowtimeSeat> findByShowtimeIdWithSeat(@Param("showtimeId") Long showtimeId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT ss FROM ShowtimeSeat ss WHERE ss.showtime.id = :showtimeId AND ss.seat.id = :seatId")
        Optional<ShowtimeSeat> findByShowtimeIdAndSeatIdWithLock(
                        @Param("showtimeId") Long showtimeId,
                        @Param("seatId") Long seatId);

        @Query("SELECT ss FROM ShowtimeSeat ss " +
                        "LEFT JOIN FETCH ss.orderDetail od " +
                        "LEFT JOIN FETCH od.order " +
                        "WHERE ss.status = 'HOLD' AND ss.holdUntil < :now")
        List<ShowtimeSeat> findExpiredHolds(@Param("now") LocalDateTime now);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("DELETE FROM ShowtimeSeat ss WHERE ss.showtime.id = :showtimeId")
        void deleteByShowtimeId(@Param("showtimeId") Long showtimeId);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("DELETE FROM ShowtimeSeat ss WHERE ss.seat.id = :seatId")
        void deleteBySeatId(@Param("seatId") Long seatId);

        boolean existsByShowtimeIdAndSeatId(
                        Long showtimeId,
                        Long seatId);
}