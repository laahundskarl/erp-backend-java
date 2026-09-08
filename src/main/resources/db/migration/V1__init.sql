CREATE TABLE catalog_item (
    id              UUID PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(1000),
    type            VARCHAR(20)     NOT NULL CHECK (type IN ('PRODUCT', 'SERVICE')),
    price           NUMERIC(19, 2)  NOT NULL CHECK (price >= 0),
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE orders (
    id                      UUID PRIMARY KEY,
    created_at              TIMESTAMP       NOT NULL,
    status                  VARCHAR(20)     NOT NULL CHECK (status IN ('OPEN', 'CLOSED')),
    discount_percentage     NUMERIC(5, 2)   NOT NULL DEFAULT 0 CHECK (discount_percentage BETWEEN 0 AND 100),
    notes                   VARCHAR(1000)
);

CREATE TABLE order_item (
    id                  UUID PRIMARY KEY,
    order_id            UUID            NOT NULL REFERENCES orders (id),
    catalog_item_id     UUID            NOT NULL REFERENCES catalog_item (id),
    quantity            INTEGER         NOT NULL CHECK (quantity > 0),
    unit_price          NUMERIC(19, 2)  NOT NULL CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);
CREATE INDEX idx_order_item_catalog_item_id ON order_item (catalog_item_id);
CREATE INDEX idx_catalog_item_name ON catalog_item (name);
CREATE INDEX idx_orders_status ON orders (status);
