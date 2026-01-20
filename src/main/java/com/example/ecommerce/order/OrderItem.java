package com.example.ecommerce.order;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private String productId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
}
