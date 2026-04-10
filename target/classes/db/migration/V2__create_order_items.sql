CREATE TABLE orders.order_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID         NOT NULL REFERENCES orders.orders(id) ON DELETE CASCADE,
    product_id  VARCHAR(255) NOT NULL,
    quantity    INT          NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(15,2) NOT NULL
);
