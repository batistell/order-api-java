package com.batistell.orderapi.service;

import com.batistell.orderapi.model.OutboxEvent;
import com.batistell.orderapi.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherJob {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> events = repository.findByPublishedFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) return;

        log.info("Found {} unpublished outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                // Publish to Kafka topic 'orders'
                kafkaTemplate.send("orders", event.getAggregateId(), event.getPayload()).get();
                
                // Mark as published
                event.setPublished(true);
                repository.save(event);
                log.info("Published event for aggregate ID: {}", event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event ID: {}", event.getId(), e);
                // Will rollback transaction for this event processing or skip to next try later
                // based on spring transaction settings, but here we just catch to not fail the whole batch
                // Actually if one fails, we probably should break or let transaction rollback the whole batch
                break;
            }
        }
    }
}
