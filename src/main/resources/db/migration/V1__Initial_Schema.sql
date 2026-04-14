CREATE TABLE products (
    product_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    restock_threshold INT DEFAULT 20,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE inventory (
    inventory_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(product_id),
    quantity_on_hand INT NOT NULL,
    warehouse_zone VARCHAR(50),
    last_updated TIMESTAMP DEFAULT NOW()
);

CREATE TABLE sales (
    sale_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(product_id),
    quantity_sold INT NOT NULL,
    sale_date DATE NOT NULL,
    revenue DECIMAL(12,2) NOT NULL,
    channel VARCHAR(50)
);

CREATE TABLE reviews (
    review_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(product_id),
    review_text TEXT NOT NULL,
    rating SMALLINT CHECK (rating >= 1 AND rating <= 5),
    sentiment_score DECIMAL(3,2),
    review_date DATE NOT NULL
);

CREATE TABLE orders (
    order_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(100),
    order_date TIMESTAMP DEFAULT NOW(),
    status VARCHAR(50)
);

CREATE TABLE delivery_routes (
    route_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(order_id),
    waypoints JSONB NOT NULL,
    total_distance_km DECIMAL(6,2),
    estimated_time_min INT,
    status VARCHAR(50)
);
