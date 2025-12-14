CREATE TABLE inventory_item (
                                id BIGSERIAL PRIMARY KEY,

                                menu_item_id VARCHAR(50) NOT NULL,
                                variant_id VARCHAR(50) NOT NULL,

                                total_quantity INT NOT NULL,
                                available_quantity INT NOT NULL,
                                reserved_quantity INT NOT NULL,

                                version BIGINT,

                                UNIQUE (menu_item_id, variant_id)
);

CREATE INDEX idx_inventory_lookup
    ON inventory_items (restaurant_id, item_id);
