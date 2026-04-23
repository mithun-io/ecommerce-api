package com.ecommerce.service;

import com.ecommerce.dto.response.*;

import java.util.List;

public interface CustomerService {

    PageResponse<ProductResponse> getProducts(String name, String category, int page, int size, String sortBy, boolean desc, String lowerRange, String higherRange);

    CartResponse addToCart(Long id, String email, String size, Double price);

    CartResponse removeFromCart(Long productId, String email, String size);

    List<CartItemResponse> viewCart(String email);

    PaymentResponse buyFromCart(String email, String address);

    PaymentResponse confirmPayment(String orderId, String razorpayId);

    List<OrdersResponse> getAllOrders(String email);
}
