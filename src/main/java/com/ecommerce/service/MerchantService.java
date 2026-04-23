package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Product;

import java.util.List;

public interface MerchantService {

    List<ProductResponse> addProducts(String email);

    ProductResponse saveProduct(ProductRequest productRequest, String email);

    List<ProductResponse> getProducts(String email);

    ProductResponse updateProduct(Long id, ProductRequest productRequest, String email);

    void deleteProduct(Long id, String email);

    ProductResponse mapToResponse(Product product);
}
