package com.example.ecommerce.product;

import com.example.ecommerce.exception.ProductNotFoundException;
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
public class ProductServiceImpl implements ProductService{
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
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
    public ProductResponse updateProduct(String id, ProductRequest request) {
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
    public Page<ProductResponse> getProducts(String search, Pageable pageable) {
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


    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .category(product.getCategory())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
