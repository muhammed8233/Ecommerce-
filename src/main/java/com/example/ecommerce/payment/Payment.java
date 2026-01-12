package com.example.ecommerce.payment;

import com.example.ecommerce.order.Order;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String orderId;

    @PositiveOrZero(message = "amount must be > 0")
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime time;

    @Indexed(unique = true)
    private String reference;
}
