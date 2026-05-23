package com.tranvanluan.backend.scheduler;

import com.tranvanluan.backend.entity.ShowtimeSeat.SeatStatus;
import com.tranvanluan.backend.repository.ShowtimeSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.tranvanluan.backend.entity.Order;
import com.tranvanluan.backend.repository.OrderRepository;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldSeatScheduler {

    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final OrderRepository orderRepository;
    private final com.tranvanluan.backend.repository.OrderDetailRepository orderDetailRepository;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void releaseExpiredHolds() {
        var expired = showtimeSeatRepository.findExpiredHolds(LocalDateTime.now());

        if (expired.isEmpty())
            return;

        java.util.Set<Long> orderIdsToDelete = new java.util.HashSet<>();

        expired.forEach(ss -> {
            if (ss.getOrderDetail() != null) {
                Order order = ss.getOrderDetail().getOrder();
                if ("PENDING".equals(order.getStatus())) {
                    orderIdsToDelete.add(order.getId());
                }
                ss.setOrderDetail(null);
            }

            ss.setStatus(SeatStatus.AVAILABLE);
            ss.setHoldUntil(null);
            ss.setUser(null);
        });

        showtimeSeatRepository.saveAll(expired);
        
        if (!orderIdsToDelete.isEmpty()) {
            java.util.List<Long> ids = new java.util.ArrayList<>(orderIdsToDelete);
            orderDetailRepository.deleteByOrderIdIn(ids);
            orderRepository.deleteByIdIn(ids);
        }
        log.info("Released {} expired held seat(s) and deleted {} pending order(s)", expired.size(), orderIdsToDelete.size());
    }
}