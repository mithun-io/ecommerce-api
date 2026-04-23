package com.ecommerce.service.impl;

import com.ecommerce.dto.request.DummyJsonRequest;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.DummyJsonResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.enums.ProductStatus;
import com.ecommerce.exception.NoResourceFoundException;
import com.ecommerce.entity.Merchant;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.MerchantRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.MerchantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final ProductRepository productRepository;

    private final RestClient restClient;
    // private final RestTemplate restTemplate;

    @Override
    public List<ProductResponse> addProducts(String email) {
        Merchant merchant = merchantRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("no merchants found"));
        DummyJsonResponse dummyJsonProductResponse = restClient.get()
                .uri("https://dummyjson.com/products")
                .retrieve()
                .body(DummyJsonResponse.class);
        // DummyJsonProductResponse response = restTemplate.getForObject("https://dummyjson.com/products", DummyJsonProductResponse.class);
        List<Product> products = new ArrayList<>();
        for (DummyJsonRequest dummyJsonProduct : dummyJsonProductResponse.getProducts()) {
            Product product = Product.builder()
                    .name(dummyJsonProduct.getTitle())
                    .category(dummyJsonProduct.getCategory())
                    .price(dummyJsonProduct.getPrice())
                    .rating(dummyJsonProduct.getRating())
                    .brand(dummyJsonProduct.getBrand() != null ? dummyJsonProduct.getBrand() : "unknown")
                    .tags(dummyJsonProduct.getTags())
                    .thumbnail(dummyJsonProduct.getThumbnail())
                    .stock(dummyJsonProduct.getStock())
                    .productStatus(ProductStatus.APPROVED)
                    .merchant(merchant)
                    .build();
            products.add(product);
        }
        List<Product> savedProducts = productRepository.saveAll(products);
        return savedProducts.stream().map(this::mapToResponse).toList();
    }

    @Override
    public ProductResponse saveProduct(ProductRequest productRequest, String email) {
        Merchant merchant = merchantRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("No merchant found"));

        Product product = Product.builder()
                .name(productRequest.getTitle())
                .category(productRequest.getCategory())
                .price(productRequest.getPrice())
                .rating(productRequest.getRating())
                .brand(productRequest.getBrand())
                .tags(productRequest.getTags())
                .thumbnail(productRequest.getThumbnail())
                .stock(productRequest.getStock())
                .productStatus(ProductStatus.APPROVED)
                .merchant(merchant)
                .build();
        Product savedProducts = productRepository.save(product);
        return mapToResponse(savedProducts);
    }

    @Override
    public List<ProductResponse> getProducts(String email) {
        Merchant merchant = merchantRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("no merchant found"));
        List<Product> products = productRepository.findByMerchant(merchant);
        if (products.isEmpty()) throw new NoResourceFoundException("no products found");
        return products.stream().map(this::mapToResponse).toList();
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, String email) {
        Merchant merchant = merchantRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("no merchant found"));
        Product product = productRepository.findByIdAndMerchant(id, merchant).orElseThrow(() -> new NoResourceFoundException("product not found"));

        product.setName(productRequest.getTitle());
        product.setCategory(productRequest.getCategory());
        product.setPrice(productRequest.getPrice());
        product.setRating(productRequest.getRating());
        product.setBrand(productRequest.getBrand());
        product.setTags(productRequest.getTags());
        product.setThumbnail(productRequest.getThumbnail());
        product.setStock(productRequest.getStock());

        Product updatedProducts = productRepository.save(product);
        return mapToResponse(updatedProducts);
    }

    @Override
    public void deleteProduct(Long id, String email) {
        Merchant merchant = merchantRepository.findByEmail(email).orElseThrow(() -> new NoResourceFoundException("no merchant found"));
        Product product = productRepository.findByIdAndMerchant(id, merchant).orElseThrow(() -> new NoResourceFoundException("product not found"));
        productRepository.delete(product);
    }

    @Override
    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .rating(product.getRating())
                .brand(product.getBrand())
                .tags(product.getTags())
                .thumbnail(product.getThumbnail())
                .stock(product.getStock())
                .productStatus(product.getProductStatus())
                .merchantId(product.getMerchant().getId())
                .build();
    }
}