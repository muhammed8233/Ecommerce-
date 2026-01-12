package com.example.ecommerce.inventory;

import com.example.ecommerce.product.Product;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "inventory_movements")
public class InventoryMovement {

    @Id
    private String id;

    @DBRef
    @NotNull(message = "product is required")
    private Product product;

    @PositiveOrZero
    private int quantityChange;

    @NotNull(message = "reason is required")
    private Reason reason;

    @CreatedDate
    @Field(name = "created_at")
    private LocalDateTime createdAt;
}

