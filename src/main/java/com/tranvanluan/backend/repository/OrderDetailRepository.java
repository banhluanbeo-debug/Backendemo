package com.tranvanluan.backend.repository;

import com.tranvanluan.backend.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // lấy tất cả order details của 1 suất chiếu (qua showtimeSeat)
    List<OrderDetail> findByShowtimeSeat_ShowtimeId(Long showtimeId);

    // check ghế đã đặt chưa trong suất đó (qua showtimeSeat)
    boolean existsByShowtimeSeat_Id(Long showtimeSeatId);

    boolean existsByShowtimeSeat_SeatId(Long seatId);
    boolean existsByShowtimeSeat_ShowtimeId(Long showtimeId);
    boolean existsByShowtimeSeat_Seat_RoomId(Long roomId);
    boolean existsByShowtimeSeat_Showtime_MovieId(Long movieId);
    boolean existsByShowtimeSeat_Seat_Room_CinemaId(Long cinemaId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM OrderDetail od WHERE od.order.id IN :orderIds")
    void deleteByOrderIdIn(@org.springframework.data.repository.query.Param("orderIds") List<Long> orderIds);
}