package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.dto.CreateOrderRequestDTO;
import com.tranvanluan.backend.dto.OrderResponseDTO;
import com.tranvanluan.backend.entity.Order;
import com.tranvanluan.backend.service.OrderService;
import com.tranvanluan.backend.service.VietQrService;
import com.tranvanluan.backend.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final VietQrService vietQrService;

    @GetMapping
    public List<OrderResponseDTO> getAllOrders() {
        return orderService.getAll()
                .stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public OrderResponseDTO getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        return OrderMapper.toDTO(order);
    }

    @GetMapping("/{id}/vietqr")
    public String getQr(@PathVariable Long id) {
        Order order = orderService.getById(id);
        return vietQrService.generateQr(order);
    }

    @PostMapping
    public OrderResponseDTO createOrder(@RequestBody CreateOrderRequestDTO request) {
        System.out.println(">>> request: " + request); // thêm dòng này

        Order order = orderService.createOrder(request);
        return OrderMapper.toDTO(order);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponseDTO> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getByUserId(userId);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponseDTO confirmOrder(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Long> body) {

        Long userId = (body != null) ? body.get("userId") : null;
        Order order = orderService.confirmOrder(id, userId);
        return OrderMapper.toDTO(order);
    }
}
