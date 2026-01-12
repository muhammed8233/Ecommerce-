package com.example.ecommerce.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @NotBlank(message = "product name can not be empty")
    @Size(min = 2, message = "product name size must be > 2")
    private String productName;

    @NotBlank(message = "description can not be empty")
    private String description;

    @NotBlank(message = "sku can not be empty")
    @Indexed(unique = true)
    private String sku;

    @PositiveOrZero(message = "price must be positive")
    @Field(targetType = org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128)
    private BigDecimal price;

    @PositiveOrZero(message = "stock quantity must be positive")
    private int stockQuantity;

    @NotBlank(message = "category can not be empty")
    private String category;

    @Version
    private int version;
}
