package com.ecommerce.kafka.consumer;

import com.ecommerce.helper.EmailService;
import com.ecommerce.kafka.event.ProductEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerService {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-success", groupId = "ecommerce-group")
    public void consume(String message) {
        try {
            PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
            log.info("kafka received: {}", paymentEvent);
            emailService.sendPaymentConfirmation(paymentEvent.getEmail(), paymentEvent.getOrderId(), paymentEvent.getAmount());
        } catch (Exception e) {
            log.error("failed to process kafka message: {}", message, e);
        }
    }

    @KafkaListener(topics = "product-topic", groupId = "ecommerce-group")
    public void consumeProduct(String message) {
        try {
            ProductEvent event = objectMapper.readValue(message, ProductEvent.class);
            log.info("product kafka received: {}", event);

            switch (event.getEventType()) {

                case "PRODUCT_CREATED":
                    notifyAdmin(event);
                    break;

                case "PRODUCT_APPROVED":
                    sendMerchantEmail(event);
                    break;

                default:
                    log.warn("Unknown product event: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("failed to process product kafka message: {}", message, e);
        }
    }

    private void sendMerchantEmail(ProductEvent event) {

        if (event.getMerchantEmail() == null) {
            log.error("merchant email is null for event: {}", event);
            return;
        }

        emailService.sendProductApprovedEmail(event.getMerchantEmail(), event.getTitle());
    }

    private void notifyAdmin(ProductEvent event) {
        emailService.notifyAdminNewProduct(event.getTitle(), event.getProductId());
    }
}