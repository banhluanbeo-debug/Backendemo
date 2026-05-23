    package com.tranvanluan.backend.repository;

    import com.tranvanluan.backend.entity.Order;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;

    import java.util.List;

    public interface OrderRepository extends JpaRepository<Order, Long> {
        List<Order> findByUserId(Long userId);

        List<Order> findByStatus(String status);

        @Query("SELECT DISTINCT o FROM Order o JOIN o.orderDetails od JOIN od.showtimeSeat ss WHERE ss.showtime.id = :showtimeId")
        List<Order> findByShowtimeId(@Param("showtimeId") Long showtimeId);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("DELETE FROM Order o WHERE o.id IN :orderIds")
        void deleteByIdIn(@org.springframework.data.repository.query.Param("orderIds") List<Long> orderIds);
    }
