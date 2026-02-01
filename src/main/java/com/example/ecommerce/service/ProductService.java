package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.dtos.ProductRequest;
import com.example.ecommerce.dtos.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(String id, ProductRequest request);

    Product findProductById(String productId);

    Page<ProductResponse> getProducts(String search, Pageable pageable);
}
