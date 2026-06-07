package com.example.payment.api.service.impl;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.payment.api.entity.OutboxEvent;
import com.example.payment.api.kafka.PaymentEventProducer;
import com.example.payment.api.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final PaymentEventProducer paymentEventProducer;
    private final OutboxRepository outboxRepository;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishEvents() {

        List<OutboxEvent> events = outboxRepository.findByProcessedFalse();
        
        for (OutboxEvent event : events) {
            log.info("Publishing payment event. paymentId={}", event.getAggregateId());
            
            try {
                paymentEventProducer.publish(event);
                event.setProcessed(true);
            } catch (Exception ex) {
                log.error( "Failed to publish outbox event. eventId={}, paymentId={}", event.getId(), event.getAggregateId(), ex);
            }

            log.info("Payment event published successfully. paymentId={}", event.getAggregateId());
        }
    }
}