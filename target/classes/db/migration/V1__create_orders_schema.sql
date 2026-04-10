CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE orders.orders (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(255) NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    total_price NUMERIC(15,2),
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
