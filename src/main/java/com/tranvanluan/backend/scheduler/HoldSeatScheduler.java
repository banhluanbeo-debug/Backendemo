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
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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

        Set<Long> orderIdsToDelete = new HashSet<>();

        expired.forEach(ss -> {

            if (ss.getOrderDetail() != null) {

                Order order = ss.getOrderDetail().getOrder();

                if ("PENDING".equals(order.getStatus())
                        || "PENDING_PAYMENT".equals(order.getStatus())) {

                    orderIdsToDelete.add(order.getId());
                }

                // bỏ liên kết để trả ghế
                ss.setOrderDetail(null);
            }

            ss.setStatus(SeatStatus.AVAILABLE);
            ss.setHoldUntil(null);
            ss.setUser(null);
        });

        showtimeSeatRepository.saveAll(expired);

        if (!orderIdsToDelete.isEmpty()) {

            List<Long> ids = new ArrayList<>(orderIdsToDelete);

            orderDetailRepository.deleteByOrderIdIn(ids);
            orderRepository.deleteByIdIn(ids);
        }

        log.info(
                "Released {} expired held seat(s) and deleted {} pending order(s)",
                expired.size(),
                orderIdsToDelete.size());
    }
}