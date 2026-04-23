package com.ecommerce.dto.response;

import com.ecommerce.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OrdersResponse {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Double amount;

    private String address;

    private String orderId;

    private String paymentId;

    private PaymentStatus paymentStatus;

    private List<CartItemResponse> items;
}