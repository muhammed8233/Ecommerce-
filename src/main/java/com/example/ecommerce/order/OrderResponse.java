package com.example.ecommerce.order;



import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderId;
    private List<OrderItemResponse> items;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Status status;
    private LocalDateTime createdAt;
    private String trackingNumber;
}
