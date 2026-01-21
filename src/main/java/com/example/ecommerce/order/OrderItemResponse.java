package com.example.ecommerce.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class OrderItemResponse {
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}