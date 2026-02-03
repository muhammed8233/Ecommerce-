package com.example.ecommerce.model;

import com.example.ecommerce.Enum.PaymentStatus;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payments")
public class Payment extends Audit{
    @Id
    private String id;

    @Indexed
    private String orderId;

    @PositiveOrZero(message = "amount must be > 0")
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private LocalDateTime time;

    @Indexed(unique = true)
    private String reference;
}
