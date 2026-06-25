package com.tranvanluan.backend.service;

import com.tranvanluan.backend.dto.CreateOrderRequestDTO;
import com.tranvanluan.backend.entity.Order;
import java.util.List;

public interface OrderService {
    List<Order> getAll();

    List<com.tranvanluan.backend.dto.OrderResponseDTO> getByUserId(Long userId);

    Order getById(Long id);

    Order create(Order order);

    Order update(Long id, Order order);

    void delete(Long id);

    Order createOrder(CreateOrderRequestDTO request);

    Order confirmOrder(Long orderId, Long userId);

    com.tranvanluan.backend.dto.OrderResponseDTO getPendingPaymentOrder(Long userId);
}
