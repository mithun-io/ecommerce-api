package com.kafka.consumer;

import com.ecommerce.helper.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.event.PaymentEvent;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerService {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-success", groupId = "ecommerce-group")
    public void consume(String message) throws JsonProcessingException, MessagingException, UnsupportedEncodingException {
        PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
        log.info("kafka received: {}", paymentEvent);
        emailService.sendPaymentConfirmation(paymentEvent.getEmail(), paymentEvent.getOrderId(), paymentEvent.getAmount());
    }
}