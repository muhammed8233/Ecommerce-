package com.example.ecommerce.product;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String productId;
    private String productName;
    private String sku;
    private String description;
    private String category;
    private BigDecimal price;
    private int stockQuantity;
    private boolean isOutOfStock;
}
