package com.tranvanluan.backend.service;

import com.tranvanluan.backend.dto.statistics.DailyStatDTO;
import com.tranvanluan.backend.dto.statistics.MovieStatDTO;
import com.tranvanluan.backend.dto.statistics.ShowtimeStatDTO;
import com.tranvanluan.backend.entity.Order;
import com.tranvanluan.backend.entity.OrderHistory;
import com.tranvanluan.backend.entity.Showtime;
import com.tranvanluan.backend.repository.OrderHistoryRepository;
import com.tranvanluan.backend.repository.OrderRepository;
import com.tranvanluan.backend.repository.ShowtimeRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ShowtimeRepository showtimeRepository;

    @Data
    @AllArgsConstructor
    private static class RawStat {
        private Long movieId;
        private String movieTitle;
        private LocalDate showDate;
        private LocalTime showTime;
        private String roomName;
        private int ticketCount;
        private double totalAmount;
        private double foodTotal;
        private double discountAmount;
    }

    private List<RawStat> getAllPaidStats() {
        List<RawStat> stats = new ArrayList<>();

        List<Order> activeOrders = orderRepository.findByStatus("PAID");
        for (Order o : activeOrders) {
            if (o.getOrderDetails() == null || o.getOrderDetails().isEmpty())
                continue;
            Showtime st = o.getOrderDetails().get(0).getShowtimeSeat().getShowtime();

            stats.add(new RawStat(
                    st.getMovie().getId(),
                    st.getMovie().getTitle(),
                    st.getShowDate(),
                    st.getShowTime(),
                    st.getRoom().getName(),
                    o.getOrderDetails().size(),
                    o.getTotalAmount(),
                    o.getFoodTotal() != null ? o.getFoodTotal() : 0.0,
                    o.getDiscountAmount() != null ? o.getDiscountAmount() : 0.0));
        }

        List<OrderHistory> historyOrders = orderHistoryRepository.findByStatus("PAID");
        for (OrderHistory h : historyOrders) {
            Showtime st = showtimeRepository.findById(h.getShowtimeId()).orElse(null);
            
            Long movieId = st != null ? st.getMovie().getId() : (long) h.getMovieTitle().hashCode();
            String title = st != null ? st.getMovie().getTitle() : h.getMovieTitle();
            LocalDate sDate = st != null ? st.getShowDate() : h.getShowDate();
            LocalTime sTime = st != null ? st.getShowTime() : h.getShowTime();
            String rName = st != null ? st.getRoom().getName() : h.getRoomName();

            int tickets = h.getSeatCodes() != null ? h.getSeatCodes().split(",").length : 0;
            stats.add(new RawStat(
                    movieId,
                    title,
                    sDate,
                    sTime,
                    rName,
                    tickets,
                    h.getTotalAmount(),
                    h.getFoodTotal() != null ? h.getFoodTotal() : 0.0,
                    h.getDiscountAmount() != null ? h.getDiscountAmount() : 0.0));
        }

        return stats;
    }

    public List<MovieStatDTO> getMonthlyStatsByMovie(int month, int year) {
        return getAllPaidStats().stream()
                .filter(s -> s.getShowDate().getMonthValue() == month && s.getShowDate().getYear() == year)
                .collect(Collectors.groupingBy(
                        RawStat::getMovieId,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            RawStat first = list.get(0);
                            int totalTickets = list.stream().mapToInt(RawStat::getTicketCount).sum();
                            double totalAmount = list.stream().mapToDouble(RawStat::getTotalAmount).sum();
                            double totalFood = list.stream().mapToDouble(RawStat::getFoodTotal).sum();
                            double totalDiscount = list.stream().mapToDouble(RawStat::getDiscountAmount).sum();
                            return new MovieStatDTO(first.getMovieId(), first.getMovieTitle(), totalTickets,
                                    totalAmount, totalFood, totalDiscount);
                        })))
                .values().stream()
                .sorted((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount())) // Sắp xếp doanh thu giảm dần
                .toList();
    }

    public List<DailyStatDTO> getDailyStatsForMovie(Long movieId, int month, int year) {
        return getAllPaidStats().stream()
                .filter(s -> s.getMovieId().equals(movieId)
                        && s.getShowDate().getMonthValue() == month
                        && s.getShowDate().getYear() == year)
                .collect(Collectors.groupingBy(
                        RawStat::getShowDate,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            int totalTickets = list.stream().mapToInt(RawStat::getTicketCount).sum();
                            double totalAmount = list.stream().mapToDouble(RawStat::getTotalAmount).sum();
                            double totalFood = list.stream().mapToDouble(RawStat::getFoodTotal).sum();
                            double totalDiscount = list.stream().mapToDouble(RawStat::getDiscountAmount).sum();
                            return new DailyStatDTO(list.get(0).getShowDate(), totalTickets, totalAmount, totalFood, totalDiscount);
                        })))
                .values().stream()
                .sorted((a, b) -> a.getShowDate().compareTo(b.getShowDate())) 
                .toList();
    }

    public List<ShowtimeStatDTO> getShowtimeStatsForMovieAndDate(Long movieId, LocalDate date) {
        return getAllPaidStats().stream()
                .filter(s -> s.getMovieId().equals(movieId) && s.getShowDate().equals(date))
                .collect(Collectors.groupingBy(
                        s -> s.getShowTime() + "_" + s.getRoomName(), // Nhóm theo giờ + phòng
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            RawStat first = list.get(0);
                            int totalTickets = list.stream().mapToInt(RawStat::getTicketCount).sum();
                            double totalAmount = list.stream().mapToDouble(RawStat::getTotalAmount).sum();
                            double totalFood = list.stream().mapToDouble(RawStat::getFoodTotal).sum();
                            double totalDiscount = list.stream().mapToDouble(RawStat::getDiscountAmount).sum();
                            return new ShowtimeStatDTO(first.getShowTime(), first.getRoomName(), totalTickets,
                                    totalAmount, totalFood, totalDiscount);
                        })))
                .values().stream()
                .sorted((a, b) -> a.getShowTime().compareTo(b.getShowTime())) // Sắp xếp tăng dần theo giờ
                .toList();
    }

    public List<com.tranvanluan.backend.dto.statistics.UserHistoryDTO> getUserHistory(Long userId) {
        List<com.tranvanluan.backend.dto.statistics.UserHistoryDTO> userHistory = new ArrayList<>();

        // 1. Lấy từ Order hiện tại
        List<Order> activeOrders = orderRepository.findByUserId(userId);
        for (Order o : activeOrders) {
            if (o.getOrderDetails() == null || o.getOrderDetails().isEmpty()) continue;
            Showtime st = o.getOrderDetails().get(0).getShowtimeSeat().getShowtime();
            userHistory.add(new com.tranvanluan.backend.dto.statistics.UserHistoryDTO(
                    st.getMovie().getTitle(),
                    st.getMovie().getPosterUrl(),
                    st.getShowDate(),
                    st.getShowTime(),
                    o.getOrderDetails().size(),
                    o.getTotalAmount(),
                    o.getStatus()
            ));
        }

        // 2. Lấy từ OrderHistory (lịch sử)
        List<OrderHistory> historyOrders = orderHistoryRepository.findByUserId(userId);
        for (OrderHistory h : historyOrders) {
            Showtime st = showtimeRepository.findById(h.getShowtimeId()).orElse(null);
            String posterUrl = st != null ? st.getMovie().getPosterUrl() : null;
            int tickets = h.getSeatCodes() != null ? h.getSeatCodes().split(",").length : 0;
            userHistory.add(new com.tranvanluan.backend.dto.statistics.UserHistoryDTO(
                    h.getMovieTitle(),
                    posterUrl,
                    h.getShowDate(),
                    h.getShowTime(),
                    tickets,
                    h.getTotalAmount(),
                    h.getStatus()
            ));
        }

        // Sắp xếp giảm dần theo ngày giờ chiếu
        userHistory.sort((a, b) -> {
            int dateCmp = b.getShowDate().compareTo(a.getShowDate());
            if (dateCmp != 0) return dateCmp;
            return b.getShowTime().compareTo(a.getShowTime());
        });

        return userHistory;
    }
}
