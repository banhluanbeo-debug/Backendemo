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

        for (Order o : activeOrders) {
            results.add(com.tranvanluan.backend.service.mapper.OrderMapper.toDTO(o));
        }

        for (OrderHistory h : historyOrders) {
            results.add(com.tranvanluan.backend.service.mapper.OrderMapper.toDTO(h));
        }

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

    

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra xem user có đơn hàng PENDING_PAYMENT nào chưa hết hạn không
        java.util.Optional<Order> pendingOpt = orderRepository.findTopByUserIdAndStatusAndExpiredAtAfter(
                user.getId(), "PENDING_PAYMENT", LocalDateTime.now());
        if (pendingOpt.isPresent()) {
            throw new RuntimeException("Bạn đang có một hóa đơn chưa thanh toán. Vui lòng hoàn tất hoặc chờ hóa đơn hết hạn để đặt vé mới.");
        }

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        double foodTotal = request.getFoodTotal() != null ? request.getFoodTotal() : 0.0;
        double discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0;

        Order order = Order.builder()
                .user(user)
                .status("PENDING_PAYMENT")
                .totalAmount(0.0)
                .orderDetails(new ArrayList<>())
                .paymentMethod(request.getPaymentMethod())
                .foodTotal(foodTotal)
                .discountAmount(discountAmount)
                .voucherCode(request.getVoucherCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        List<OrderDetail> details = new ArrayList<>();

        for (Long seatId : request.getSeatIds()) {

            ShowtimeSeat ss = showtimeSeatRepository
                    .findByShowtimeIdAndSeatIdWithLock(showtime.getId(), seatId)
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy ghế " + seatId + " trong suất chiếu này"));

            boolean isAvailable = ss.getStatus() == SeatStatus.AVAILABLE;
            boolean isExpiredHold = ss.isHoldExpired();

            if (!isAvailable && !isExpiredHold) {
                throw new RuntimeException(
                        "Ghế " + ss.getSeat().getCode() + " đang được giữ hoặc đã được đặt");
            }

            ss.setStatus(SeatStatus.HOLD);
            ss.setHoldUntil(LocalDateTime.now().plusMinutes(5));
            ss.setUser(user);
            showtimeSeatRepository.save(ss);

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .showtimeSeat(ss)
                    .price(showtime.getPrice())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            details.add(detail);
        }

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

   

    @Transactional
    public Order confirmOrder(Long orderId, Long userId) {
        Order order = getById(orderId);

        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xác nhận đơn hàng này");
        }

        if (!"PENDING".equals(order.getStatus()) && !"PENDING_PAYMENT".equals(order.getStatus())) {
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

        if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
            try {
                voucherService.markUsed(order.getVoucherCode());
            } catch (Exception e) {
             
                System.err.println("Không thể đánh dấu voucher USED: " + e.getMessage());
            }
        }

        return saved;
    }

    @Override
    public com.tranvanluan.backend.dto.OrderResponseDTO getPendingPaymentOrder(Long userId) {
        java.util.Optional<Order> orderOpt = orderRepository.findTopByUserIdAndStatusAndExpiredAtAfter(
                userId, "PENDING_PAYMENT", LocalDateTime.now());
        
        if (orderOpt.isPresent()) {
            return com.tranvanluan.backend.service.mapper.OrderMapper.toDTO(orderOpt.get());
        }
        return null;
    }
}