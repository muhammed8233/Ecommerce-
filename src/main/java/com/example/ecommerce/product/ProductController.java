package com.example.ecommerce.product;


import com.example.ecommerce.inventory.InventoryMovementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryMovementService inventoryMovementService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(@RequestParam(required = false) String search,
                                                              @PageableDefault(size = 10,sort = "productName",
                                                                      direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(productService.getProducts(search, pageable));
    }
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public  ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request){
        ProductResponse create = productService.createProduct(request);
        return new ResponseEntity<>(create, HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("studentId") String productId,
                                                         @Valid @RequestBody ProductRequest request){
        ProductResponse update = productService.updateProduct(productId, request);
        return ResponseEntity.ok(update);
    }

    @PutMapping("/{productId}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restockProduct(@PathVariable String productId,
                                                 @RequestParam int quantity){
        inventoryMovementService.restockProduct(productId, quantity);
        return ResponseEntity.ok("product restock successfully");
    }
}
