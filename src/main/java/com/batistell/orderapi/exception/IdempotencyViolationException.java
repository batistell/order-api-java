package com.batistell.orderapi.exception;

import java.util.UUID;

public class IdempotencyViolationException extends RuntimeException {
    private final UUID cachedOrderId;

    public IdempotencyViolationException(UUID cachedOrderId) {
        super("Idempotency key already processed");
        this.cachedOrderId = cachedOrderId;
    }

    public UUID getCachedOrderId() {
        return cachedOrderId;
    }
}
