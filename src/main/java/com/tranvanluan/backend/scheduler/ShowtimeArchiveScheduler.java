package com.tranvanluan.backend.scheduler;

import com.tranvanluan.backend.entity.*;
import com.tranvanluan.backend.entity.ShowtimeSeat.SeatStatus;
import com.tranvanluan.backend.repository.*;
import com.tranvanluan.backend.service.RewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowtimeArchiveScheduler {

    private final ShowtimeRepository showtimeRepository;
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final RewardService rewardService;

    private final TransactionTemplate transactionTemplate;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private static final int BUFFER_MINUTES = 2;

    @Scheduled(fixedRate = 10000)
    public void archiveEndedShowtimes() {
        try (java.io.FileWriter fw = new java.io.FileWriter("scheduler_debug.log", true);
             java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {

            List<Showtime> showtimes = showtimeRepository.findAll();
            pw.println("=== Scheduler running at " + LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                    + " — total showtimes: " + showtimes.size() + " ===");

            for (Showtime showtime : showtimes) {
                boolean ended = isShowtimeEnded(showtime);
                pw.println("  Showtime id=" + showtime.getId()
                        + " date=" + showtime.getShowDate()
                        + " time=" + showtime.getShowTime()
                        + " movie=" + (showtime.getMovie() != null ? showtime.getMovie().getTitle() : "NULL")
                        + " duration=" + (showtime.getMovie() != null ? showtime.getMovie().getDuration() : "NULL")
                        + " ended=" + ended);

                if (!ended) continue;

                Set<Long> archivedUserIds = new HashSet<>();

                try {
                    transactionTemplate.execute(status -> {
                        archiveShowtime(showtime, archivedUserIds);
                        return null;
                    });
                    pw.println("    SUCCESSFULLY ARCHIVED showtime id=" + showtime.getId());
                } catch (Exception e) {
                    pw.println("    FAILED to archive showtime id=" + showtime.getId() + " error: " + e.getMessage());
                    e.printStackTrace(pw);
                    continue; 
                }

                for (Long userId : archivedUserIds) {
                    try {
                        rewardService.checkAndGrantRewards(userId);
                        pw.println("    Checked rewards for userId=" + userId);
                    } catch (Exception e) {
                        pw.println("    FAILED to check rewards for userId=" + userId + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to write debug log", e);
        }
    }

    private void archiveShowtime(Showtime showtime, Set<Long> archivedUserIds) {
        log.info("Archiving showtime id={} movie='{}'",
                showtime.getId(), showtime.getMovie().getTitle());

        List<Order> orders = orderRepository.findByShowtimeId(showtime.getId());

        for (Order order : orders) {
            String seatCodes = order.getOrderDetails().stream()
                    .map(od -> od.getShowtimeSeat() != null && od.getShowtimeSeat().getSeat() != null
                            ? od.getShowtimeSeat().getSeat().getCode()
                            : "?")
                    .collect(Collectors.joining(", "));

            OrderHistory history = OrderHistory.builder()
                    .originalOrderId(order.getId())
                    .userId(order.getUser().getId())
                    .showtimeId(showtime.getId())
                    .movieTitle(showtime.getMovie().getTitle())
                    .showDate(showtime.getShowDate())
                    .showTime(showtime.getShowTime())
                    .roomName(showtime.getRoom() != null ? showtime.getRoom().getName() : null)
                    .totalAmount(order.getTotalAmount())
                    .paymentMethod(order.getPaymentMethod())
                    .status(order.getStatus())
                    .seatCodes(seatCodes)
                    .foodTotal(order.getFoodTotal())
                    .discountAmount(order.getDiscountAmount())
                    .voucherCode(order.getVoucherCode())
                    .createdAt(order.getCreatedAt())
                    .archivedAt(LocalDateTime.now())
                    .build();

            orderHistoryRepository.save(history);

            if ("PAID".equalsIgnoreCase(order.getStatus())) {
                archivedUserIds.add(order.getUser().getId());
            }

            log.info("Archived order history for order id={} seats='{}'", order.getId(), seatCodes);
        }

        orderHistoryRepository.flush();

        entityManager.clear();

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        if (!orderIds.isEmpty()) {
            orderDetailRepository.deleteByOrderIdIn(orderIds);
            orderRepository.deleteByIdIn(orderIds);
        }

        showtimeSeatRepository.deleteByShowtimeId(showtime.getId());

        showtimeRepository.deleteByShowtimeId(showtime.getId());

        log.info("Showtime id={} fully removed from DB via bulk delete.", showtime.getId());
    }

    private boolean isShowtimeEnded(Showtime showtime) {
        if (showtime.getShowDate() == null || showtime.getShowTime() == null) return false;
        if (showtime.getMovie() == null || showtime.getMovie().getDuration() == null) return false;

        LocalDateTime startTime = LocalDateTime.of(showtime.getShowDate(), showtime.getShowTime());
        LocalDateTime endTime = startTime
                .plusMinutes(showtime.getMovie().getDuration())
                .plusMinutes(BUFFER_MINUTES);

        LocalDateTime nowInVietnam = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        return nowInVietnam.isAfter(endTime);
    }
}