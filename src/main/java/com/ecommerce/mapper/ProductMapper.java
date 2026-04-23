package com.ecommerce.mapper;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toProduct(ProductRequest productRequest);

    @Mapping(target = "merchantId", expression = "java(product.getMerchant().getId())")
    ProductResponse toProductResponse(Product product);

    List<ProductResponse> toProductResponses(List<Product> products);
}
