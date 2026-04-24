package com.ecommerce.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {

    private String eventType;

    private Long productId;

    private Long merchantId;

    private String merchantEmail;

    private String title;

    private Double price;

    private String status;

    private String timestamp;
}