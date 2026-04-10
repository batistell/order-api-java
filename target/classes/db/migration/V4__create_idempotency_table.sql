CREATE TABLE orders.idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    order_id        UUID         NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
