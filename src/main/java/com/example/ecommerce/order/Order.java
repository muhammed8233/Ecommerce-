package com.example.ecommerce.order;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId;
    private Status status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    private List<OrderItem> items;


    public void addOrderItem(OrderItem orderItem) {


    }
}
