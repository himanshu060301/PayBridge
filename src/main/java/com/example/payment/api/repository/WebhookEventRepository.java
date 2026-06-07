package com.example.payment.api.repository;

import com.example.payment.api.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByWebhookId(String webhookId);
}