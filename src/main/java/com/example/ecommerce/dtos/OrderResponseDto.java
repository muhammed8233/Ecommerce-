package com.example.ecommerce.dtos;



import com.example.ecommerce.Enum.OrderedStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private String orderId;
    private List<OrderItemResponseDto> items;
    private BigDecimal totalAmount;
    private OrderedStatus orderedStatus;
}
