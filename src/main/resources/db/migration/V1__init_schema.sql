-- ============================================================================
-- V1 — Initial schema
-- Tables: customers, products, orders, order_items
-- Charset / engine left to MySQL defaults (utf8mb4 / InnoDB on MySQL 8+).
-- ============================================================================

CREATE TABLE customers (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255),
    document VARCHAR(20),
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uk_customers_email    UNIQUE (email),
    CONSTRAINT uk_customers_document UNIQUE (document)
);

CREATE TABLE products (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255)   NOT NULL,
    sku            VARCHAR(64)    NOT NULL,
    unit_price     DECIMAL(15, 2) NOT NULL,
    stock_quantity INT            NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku)
);

CREATE TABLE orders (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    customer_id  BIGINT         NOT NULL,
    created_at   DATETIME(6)    NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);

CREATE TABLE order_items (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    order_id    BIGINT         NOT NULL,
    product_id  BIGINT         NOT NULL,
    quantity    INT            NOT NULL,
    unit_price  DECIMAL(15, 2) NOT NULL,
    total_price DECIMAL(15, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders   (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_order_items_order_id   ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
