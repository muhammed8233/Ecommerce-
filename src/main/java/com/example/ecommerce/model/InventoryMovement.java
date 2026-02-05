package com.example.ecommerce.model;

import com.example.ecommerce.Enum.Reason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "inventory_movements")
public class InventoryMovement extends Audit{

    @Id
    private String id;

    @DBRef
    @NotNull(message = "product is required")
    private Product product;

    @PositiveOrZero
    private int quantityChange;

    @NotNull(message = "reason is required")
    private Reason reason;

}

