package com.example.ecommerce.service;

import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.dtos.ProductRequestDto;
import com.example.ecommerce.dtos.ProductResponseDto;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ProductResponseDto createProduct(ProductRequestDto request) {
        Product product = Product.builder()
                .productName(request.getProductName())
                .sku(request.getSku())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();
        Product saved = productRepository.save(product);

        return mapToProductResponse(saved);

    }

    @Override
    public ProductResponseDto updateProduct(String id, ProductRequestDto request) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setProductName(request.getProductName());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setStockQuantity(request.getStockQuantity());

        Product updateProduct = productRepository.save(product);

        return mapToProductResponse(updateProduct);

    }

    @Override
    public Product findProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(()-> new ProductNotFoundException("product with id"+productId+ "not found"));
    }

    @Override
    public Page<ProductResponseDto> getProducts(String search, Pageable pageable) {
        Query query = new Query().with(pageable);

        if (search != null && !search.isBlank()) {
            String regexPattern = ".*" + search + ".*";

            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("productName").regex(regexPattern, "i"),
                    Criteria.where("category").regex(regexPattern, "i")
            );

            query.addCriteria(criteria);
        }


        List<Product> products = mongoTemplate.find(query, Product.class);

        long count = mongoTemplate.count(query.skip(-1).limit(-1), Product.class);

        return PageableExecutionUtils.getPage(products, pageable, () -> count)
                .map(this::mapToProductResponse);
    }


    private ProductResponseDto mapToProductResponse(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .category(product.getCategory())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    @Override
    public Product findById(String productId){
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id: " + productId + "not found"));
    }

    @Override
    public List<Product> findAllById(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return null;
        }
        return productRepository.findAllById(productIds);
    }
}
