package com.tranvanluan.backend.repository;

import com.tranvanluan.backend.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByStatus(String status);
    List<OrderHistory> findByUserId(Long userId);
}