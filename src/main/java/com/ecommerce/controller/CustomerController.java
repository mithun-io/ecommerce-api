package com.ecommerce.controller;

import com.ecommerce.dto.response.*;
import com.ecommerce.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/products")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(@RequestParam(required = false) String name,
                                                                                  @RequestParam(required = false) String category,
                                                                                  @RequestParam(defaultValue = "1") int page,
                                                                                  @RequestParam(defaultValue = "10") int size,
                                                                                  @RequestParam(defaultValue = "id") String sort,
                                                                                  @RequestParam(defaultValue = "false") boolean desc,
                                                                                  @RequestParam(required = false) String lowerRange,
                                                                                  @RequestParam(required = false) String higherRange) {
        return ResponseEntity.ok(new ApiResponse<>(true, "products fetched successfully", customerService.getProducts(name, category, page, size, sort, desc, lowerRange, higherRange), 200));
    }

    @PostMapping("/add-product/cart/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@PathVariable() Long id,
                                                               @RequestParam String email,
                                                               @RequestParam String size,
                                                               @RequestParam Double price) {
        return ResponseEntity.ok(new ApiResponse<>(true, "product added to cart", customerService.addToCart(id, email, size, price), 200));
    }

    @DeleteMapping("/remove-product/cart/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(@PathVariable Long productId,
                                                                    @RequestParam String email,
                                                                    @RequestParam String size) {
        return ResponseEntity.ok(new ApiResponse<>(true, "product removed from cart", customerService.removeFromCart(productId, email, size), 200));
    }

    @GetMapping("/cart/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> viewCart(Principal principal) {
        return ResponseEntity.ok(new ApiResponse<>(true, "items from cart fetched successfully", customerService.viewCart(principal.getName()), 200));
    }

    @PostMapping("/cart/buy")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> buyFromCart(Principal principal, @RequestParam String email) {
        return ResponseEntity.ok(new ApiResponse<>(true, "order created successfully, please confirm the payment", customerService.buyFromCart(principal.getName(), email), 200));
    }

    @PostMapping("/payment/confirm/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(@PathVariable String orderId, @RequestParam("razorpay_payment_id") String razorpayID) {
        return ResponseEntity.ok(new ApiResponse<>(true, "payment confirmed successfully", customerService.confirmPayment(orderId, razorpayID), 200));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<OrdersResponse>>> getAllOrders(Principal principal) {
        return ResponseEntity.ok(new ApiResponse<>(true, "orders fetched successfully", customerService.getAllOrders(principal.getName()), 200));
    }
}