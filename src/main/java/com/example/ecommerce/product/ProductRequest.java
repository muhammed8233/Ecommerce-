package com.example.ecommerce.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String productName;
    private String sku;
    private String description;
    private String category;
    private BigDecimal price;
    private int stockQuantity;
}
