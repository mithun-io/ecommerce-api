package com.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler((record, exception) -> {
            log.error("failed message: {}", record.value(), exception);
        }, new FixedBackOff(2000L, 3));
    }
}
