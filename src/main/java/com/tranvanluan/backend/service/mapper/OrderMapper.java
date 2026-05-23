package com.tranvanluan.backend.service.mapper;

import com.tranvanluan.backend.dto.*;
import com.tranvanluan.backend.entity.*;

public class OrderMapper {

        public static OrderResponseDTO toDTO(Order order) {
                return OrderResponseDTO.builder()
                                .id(order.getId())
                                .totalAmount(order.getTotalAmount())
                                .status(order.getStatus())
                                .paymentMethod(order.getPaymentMethod())
                                .foodTotal(order.getFoodTotal())
                                .discountAmount(order.getDiscountAmount())
                                .voucherCode(order.getVoucherCode())
                                .createdAt(order.getCreatedAt())
                                .user(toUserDTO(order.getUser()))
                                .orderDetails(
                                                order.getOrderDetails()
                                                                .stream()
                                                                .map(OrderMapper::toOrderDetailDTO)
                                                                .toList())
                                .build();
        }

        public static OrderResponseDTO toDTO(OrderHistory history) {
                UserDTO userDTO = UserDTO.builder()
                                .id(history.getUserId())
                                .build();

                java.util.List<OrderDetailDTO> details = new java.util.ArrayList<>();
                if (history.getSeatCodes() != null && !history.getSeatCodes().trim().isEmpty()) {
                        String[] seats = history.getSeatCodes().split(",\\s*");
                        for (String seat : seats) {
                                details.add(OrderDetailDTO.builder()
                                                .seatCode(seat)
                                                .showtime(ShowtimeDTO.builder()
                                                                .movieTitle(history.getMovieTitle())
                                                                .showDate(history.getShowDate())
                                                                .showTime(history.getShowTime())
                                                                .roomName(history.getRoomName())
                                                                .build())
                                                .build());
                        }
                } else {
                        details.add(OrderDetailDTO.builder()
                                        .showtime(ShowtimeDTO.builder()
                                                        .movieTitle(history.getMovieTitle())
                                                        .showDate(history.getShowDate())
                                                        .showTime(history.getShowTime())
                                                        .roomName(history.getRoomName())
                                                        .build())
                                        .build());
                }

                return OrderResponseDTO.builder()
                                .id(history.getOriginalOrderId())
                                .totalAmount(history.getTotalAmount())
                                .status(history.getStatus())
                                .paymentMethod(history.getPaymentMethod())
                                .foodTotal(history.getFoodTotal())
                                .discountAmount(history.getDiscountAmount())
                                .voucherCode(history.getVoucherCode())
                                .seatCodes(history.getSeatCodes())
                                .movieTitle(history.getMovieTitle())
                                .createdAt(history.getCreatedAt())
                                .user(userDTO)
                                .orderDetails(details)
                                .build();
        }

        private static UserDTO toUserDTO(User user) {
                if (user == null)
                        return null;

                return UserDTO.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .build();
        }

        private static OrderDetailDTO toOrderDetailDTO(OrderDetail od) {
                ShowtimeSeat ss = od.getShowtimeSeat();

                return OrderDetailDTO.builder()
                                .id(od.getId())
                                .price(od.getPrice())
                                .seatCode(ss != null && ss.getSeat() != null ? ss.getSeat().getCode() : null)
                                .seatType(ss != null && ss.getSeat() != null ? ss.getSeat().getType() : null)
                                .showtime(ss != null ? toShowtimeDTO(ss.getShowtime()) : null)
                                .build();
        }

        private static ShowtimeDTO toShowtimeDTO(Showtime st) {
                return ShowtimeDTO.builder()
                                .id(st.getId())
                                .showDate(st.getShowDate())
                                .showTime(st.getShowTime())
                                .price(st.getPrice())

                                .movieId(
                                                st.getMovie() != null
                                                                ? st.getMovie().getId()
                                                                : null)
                                .movieTitle(
                                                st.getMovie() != null
                                                                ? st.getMovie().getTitle()
                                                                : null)

                                .roomId(
                                                st.getRoom() != null
                                                                ? st.getRoom().getId()
                                                                : null)
                                .roomName(
                                                st.getRoom() != null
                                                                ? st.getRoom().getName()
                                                                : null)
                                .build();
        }

}
