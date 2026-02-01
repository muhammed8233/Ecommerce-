package com.example.ecommerce.model;
import com.example.ecommerce.Enum.Status;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String userId;
    private Status status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItem> orderedItems;


    public void addOrderItem(OrderItem orderItem) {
        if (this.orderedItems == null) {
            this.orderedItems = new ArrayList<>();
        }
        this.orderedItems.add(orderItem);

    }
}
