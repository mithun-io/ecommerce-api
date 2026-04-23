package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/product")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<ProductResponse>> saveProduct(@Valid @RequestBody ProductRequest request, @RequestParam String email) {
        ProductResponse response = merchantService.saveProduct(request, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "product saved successfully", response, 200));
    }

    @PostMapping("/products/dummy")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> addProductsFromDummy(@RequestParam String email) {
        List<ProductResponse> responses = merchantService.addProducts(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "products added from DummyJSON", responses, 200));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(@RequestParam String email) {
        List<ProductResponse> responses = merchantService.getProducts(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "products fetched successfully", responses, 200));
    }

    @PutMapping("/product/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request, @RequestParam String email) {
        ProductResponse response = merchantService.updateProduct(id, request, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "product updated successfully", response, 200));
    }

    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id, @RequestParam String email) {
        merchantService.deleteProduct(id, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "product deleted successfully", null, 200));
    }
}
