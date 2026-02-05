package com.example.ecommerce.model;

import com.example.ecommerce.Enum.OrderedStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order extends Audit{

    @Id
    private String id;
    private String userId;
    private OrderedStatus orderedStatus;
    private BigDecimal totalAmount;
    private List<OrderItem> orderedItems;


    public void addOrderItem(OrderItem orderItem) {
        if (this.orderedItems == null) {
            this.orderedItems = new ArrayList<>();
        }
        this.orderedItems.add(orderItem);

    }
}
