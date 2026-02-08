-- Products table for item comparison.
-- Base columns plus specifications as JSON (any product type can store key-value specs).
CREATE TABLE products (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    VARCHAR(2000),
    price          DECIMAL(12, 2) NOT NULL,
    size           VARCHAR(50),
    weight         VARCHAR(50),
    color          VARCHAR(50),
    image_url      VARCHAR(500),
    rating         DECIMAL(3, 2),
    product_type   VARCHAR(50) NOT NULL DEFAULT 'GENERIC',
    specifications CLOB
);

CREATE INDEX idx_products_product_type ON products (product_type);
