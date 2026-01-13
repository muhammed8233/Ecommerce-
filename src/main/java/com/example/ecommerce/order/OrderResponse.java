package com.example.ecommerce.order;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Status status;
    private LocalDateTime createdAt;
    private String trackingNumber;
}
