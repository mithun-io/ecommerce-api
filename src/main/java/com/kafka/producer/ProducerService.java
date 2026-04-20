package com.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendPaymentEvent(PaymentEvent paymentEvent) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(paymentEvent);
        kafkaTemplate.send("payment-success", message).whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("kafka sent: {}", message);
            } else {
                log.error("kafka send failed: {}", message, exception);
            }
        });
    }
}