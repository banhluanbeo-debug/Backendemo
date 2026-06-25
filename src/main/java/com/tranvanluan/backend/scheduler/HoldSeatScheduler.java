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

        java.util.Set<Order> ordersToExpire = new java.util.HashSet<>();

        expired.forEach(ss -> {
            if (ss.getOrderDetail() != null) {
                Order order = ss.getOrderDetail().getOrder();
                if ("PENDING".equals(order.getStatus()) || "PENDING_PAYMENT".equals(order.getStatus())) {
                    order.setStatus("EXPIRED");
                    ordersToExpire.add(order);
                }
                // Do not remove order detail from seat to keep history
            }

            ss.setStatus(SeatStatus.AVAILABLE);
            ss.setHoldUntil(null);
            ss.setUser(null);
        });

        showtimeSeatRepository.saveAll(expired);
        
        if (!ordersToExpire.isEmpty()) {
            orderRepository.saveAll(ordersToExpire);
        }
        log.info("Released {} expired held seat(s) and expired {} pending order(s)", expired.size(), ordersToExpire.size());
    }
}