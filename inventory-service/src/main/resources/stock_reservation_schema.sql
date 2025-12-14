CREATE TABLE stock_reservation (
                                   id BIGSERIAL PRIMARY KEY,

                                   order_id VARCHAR(50) NOT NULL,
                                   menu_item_id VARCHAR(50) NOT NULL,
                                   variant_id VARCHAR(50) NOT NULL,

                                   quantity INT NOT NULL,
                                   status VARCHAR(20) NOT NULL, -- RESERVED, RELEASED, CONFIRMED

                                   reserved_at TIMESTAMP NOT NULL,

                                   UNIQUE (order_id, menu_item_id, variant_id)
);


CREATE INDEX idx_reservation_expiry
    ON stock_reservations (expires_at);