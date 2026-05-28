package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.dto.CreateOrderRequestDTO;
import com.tranvanluan.backend.entity.*;
import com.tranvanluan.backend.entity.ShowtimeSeat.SeatStatus;
import com.tranvanluan.backend.repository.*;
import com.tranvanluan.backend.service.OrderService;
import com.tranvanluan.backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final UserRepository userRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final VoucherService voucherService;

    // ----------------------------------------------------------------
    // Các method cũ giữ nguyên
    // ----------------------------------------------------------------

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id " + id));
    }

    @Override
    public List<com.tranvanluan.backend.dto.OrderResponseDTO> getByUserId(Long userId) {
        List<Order> activeOrders = orderRepository.findByUserId(userId);
        List<OrderHistory> historyOrders = orderHistoryRepository.findByUserId(userId);

        List<com.tranvanluan.backend.dto.OrderResponseDTO> results = new ArrayList<>();

        // Add active orders
        for (Order o : activeOrders) {
            results.add(com.tranvanluan.backend.service.mapper.OrderMapper.toDTO(o));
        }

        // Add history/archived orders
        for (OrderHistory h : historyOrders) {
            results.add(com.tranvanluan.backend.service.mapper.OrderMapper.toDTO(h));
        }

        // Sort descending by createdAt or id
        results.sort((a, b) -> {
            if (b.getCreatedAt() != null && a.getCreatedAt() != null) {
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            if (b.getId() != null && a.getId() != null) {
                return b.getId().compareTo(a.getId());
            }
            return 0;
        });

        return results;
    }

    @Override
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order update(Long id, Order order) {
        Order existing = getById(id);
        existing.setStatus(order.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existing);
    }

    // ----------------------------------------------------------------
    // createOrder — thay thế hoàn toàn, dùng ShowtimeSeat + Lock
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        // Tính foodTotal và discountAmount từ request (frontend đã tính sẵn)
        double foodTotal = request.getFoodTotal() != null ? request.getFoodTotal() : 0.0;
        double discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0;

        // Tạo Order trước (chưa có details, chưa save)
        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .totalAmount(0.0)
                .orderDetails(new ArrayList<>())
                .paymentMethod(request.getPaymentMethod())
                .foodTotal(foodTotal)
                .discountAmount(discountAmount)
                .voucherCode(request.getVoucherCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<OrderDetail> details = new ArrayList<>();

        for (Long seatId : request.getSeatIds()) {

            // 1. Lock row ShowtimeSeat lại — thread khác phải đợi transaction này xong
            ShowtimeSeat ss = showtimeSeatRepository
                    .findByShowtimeIdAndSeatIdWithLock(showtime.getId(), seatId)
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy ghế " + seatId + " trong suất chiếu này"));

            // 2. Sau khi lock rồi mới check — lúc này data đã chính xác
            boolean isAvailable = ss.getStatus() == SeatStatus.AVAILABLE;
            boolean isExpiredHold = ss.isHoldExpired();

            if (!isAvailable && !isExpiredHold) {
                throw new RuntimeException(
                        "Ghế " + ss.getSeat().getCode() + " đang được giữ hoặc đã được đặt");
            }

            // 3. Giữ ghế (HOLD) — thanh toán xong sẽ chuyển sang BOOKED
            ss.setStatus(SeatStatus.HOLD);
            ss.setHoldUntil(LocalDateTime.now().plusMinutes(2));
            ss.setUser(user);
            showtimeSeatRepository.save(ss);

            // 4. Tạo OrderDetail trỏ vào ShowtimeSeat
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .showtimeSeat(ss)
                    .price(showtime.getPrice())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            ss.setOrderDetail(detail);
            details.add(detail);
        }

        // Tổng = tiền vé + đồ ăn - voucher
        double ticketTotal = details.stream().mapToDouble(OrderDetail::getPrice).sum();
        double grandTotal = ticketTotal + foodTotal - discountAmount;
        order.setTotalAmount(grandTotal);
        order.setOrderDetails(details);

        try {
            return orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Một hoặc nhiều ghế vừa được người khác đặt, vui lòng chọn lại");
        }
    }

    // ----------------------------------------------------------------
    // confirmOrder — gọi sau khi thanh toán thành công
    // ----------------------------------------------------------------

    @Transactional
    public Order confirmOrder(Long orderId, Long userId) {
        Order order = getById(orderId);

        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xác nhận đơn hàng này");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        boolean isCash = "CASH".equalsIgnoreCase(order.getPaymentMethod());

        for (OrderDetail detail : order.getOrderDetails()) {
            ShowtimeSeat ss = detail.getShowtimeSeat();

            if (!isCash && ss.isHoldExpired()) {
                throw new RuntimeException(
                        "Ghế " + ss.getSeat().getCode() + " đã hết thời gian giữ, vui lòng đặt lại");
            }

            ss.setStatus(SeatStatus.BOOKED);
            ss.setHoldUntil(null);
            showtimeSeatRepository.save(ss);
        }

        order.setStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // Đánh dấu voucher USED — chỉ sau khi thanh toán thành công
        if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
            try {
                voucherService.markUsed(order.getVoucherCode());
            } catch (Exception e) {
                // Log nhưng không fail toàn bộ transaction
                // Voucher có thể đã USED hoặc không tồn tại
                System.err.println("Không thể đánh dấu voucher USED: " + e.getMessage());
            }
        }

        return saved;
    }
}