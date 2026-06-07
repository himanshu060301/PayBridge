package com.example.payment.api.kafka;

import com.example.payment.api.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {

    @Value("${spring.kafka.topic}")
    private String TOPIC;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(OutboxEvent event) {
        log.info("Sending event to Kafka. paymentId={}",event.getAggregateId());
        kafkaTemplate.send(
                TOPIC,
                event.getAggregateId(),
                event.getPayload()
        );
    }
}