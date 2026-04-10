package com.batistell.orderapi.service;

import com.batistell.orderapi.exception.IdempotencyViolationException;
import com.batistell.orderapi.model.IdempotencyKey;
import com.batistell.orderapi.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    @Transactional(readOnly = true)
    public void checkIdempotency(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return; // Or throw an exception if header is mandatory
        }
        Optional<IdempotencyKey> existingKey = repository.findByIdempotencyKey(idempotencyKey);
        if (existingKey.isPresent()) {
            throw new IdempotencyViolationException(existingKey.get().getOrderId());
        }
    }

    // Called within the same transaction as order creation
    public void saveIdempotencyKey(String idempotencyKey, UUID orderId) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return;
        }
        IdempotencyKey key = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .orderId(orderId)
                .build();
        repository.save(key);
    }
}
