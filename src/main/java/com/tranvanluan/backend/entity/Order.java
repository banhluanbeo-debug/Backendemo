    package com.tranvanluan.backend.entity;

    import jakarta.persistence.*;
    import lombok.*;
    import java.time.LocalDateTime;
    import java.util.List;

    import com.fasterxml.jackson.annotation.JsonManagedReference;

    @Entity
    @Table(name = "orders")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private Double totalAmount;
        private String status = "PENDING";

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference
        private List<OrderDetail> orderDetails;

        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        private String paymentMethod;

        private Double foodTotal = 0.0;      // tổng tiền đồ ăn
        private Double discountAmount = 0.0; // số tiền voucher giảm
        private String voucherCode;           // mã voucher đã dùng (nullable)

        // Safe equals/hashCode — only use ID
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Order))
                return false;
            Order other = (Order) o;
            return id != null && id.equals(other.id);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }