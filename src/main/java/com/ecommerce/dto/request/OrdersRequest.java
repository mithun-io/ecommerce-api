package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrdersRequest {

    @NotNull(message = "amount is required")
    private Double amount;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "order id is required")
    private String orderId;

    @NotNull(message = "payment id is required")
    private String paymentId;

    @NotEmpty(message = "items are required")
    private List<CartItemRequest> items;
}