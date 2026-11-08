CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(254) NOT NULL,
    google_sub VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_google_sub (google_sub),
    CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'OPERATOR'))
) ENGINE=InnoDB;

CREATE TABLE flavors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flavors_name (name),
    CONSTRAINT chk_flavors_price CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE TABLE ingredients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    stock_qty DECIMAL(12,3) NOT NULL,
    low_stock_threshold DECIMAL(12,3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ingredients_name (name),
    CONSTRAINT chk_ingredients_stock CHECK (stock_qty >= 0),
    CONSTRAINT chk_ingredients_threshold CHECK (low_stock_threshold >= 0)
) ENGINE=InnoDB;

CREATE TABLE flavor_ingredients (
    flavor_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    qty_per_serving DECIMAL(12,3) NOT NULL,
    PRIMARY KEY (flavor_id, ingredient_id),
    CONSTRAINT fk_fi_flavor FOREIGN KEY (flavor_id) REFERENCES flavors(id),
    CONSTRAINT fk_fi_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id),
    CONSTRAINT chk_fi_qty CHECK (qty_per_serving > 0)
) ENGINE=InnoDB;

CREATE TABLE stops (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_stops_started_at (started_at),
    CONSTRAINT chk_stops_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_stops_longitude CHECK (longitude BETWEEN -180 AND 180)
) ENGINE=InnoDB;

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    stop_id BIGINT NULL,
    status VARCHAR(16) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_orders_created_at (created_at),
    KEY idx_orders_status_created_at (status, created_at),
    KEY idx_orders_stop_created_at (stop_id, created_at),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_stop FOREIGN KEY (stop_id) REFERENCES stops(id) ON DELETE SET NULL,
    CONSTRAINT chk_orders_total CHECK (total >= 0),
    CONSTRAINT chk_orders_status CHECK (status IN ('PENDING', 'PREPARING', 'READY', 'SERVED', 'CANCELLED'))
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    flavor_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_items_flavor (flavor_id),
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_flavor FOREIGN KEY (flavor_id) REFERENCES flavors(id),
    CONSTRAINT chk_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_items_unit_price CHECK (unit_price >= 0)
) ENGINE=InnoDB;
