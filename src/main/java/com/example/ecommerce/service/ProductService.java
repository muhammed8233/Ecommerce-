package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.dtos.ProductRequestDto;
import com.example.ecommerce.dtos.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto request);

    ProductResponseDto updateProduct(String id, ProductRequestDto request);

    Product findProductById(String productId);

    Page<ProductResponseDto> getProducts(String search, Pageable pageable);

    Product findById(String productId);

    List<Product> findAllById(List<String> productIds);

    void deleteAll();

    void increaseStock(String productId, Integer quantity);

    void deductStock(String productId, int quantity);

    Product saveProduct(Product product);
}

