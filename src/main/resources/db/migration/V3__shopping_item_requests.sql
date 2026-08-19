-- Phase 4 member approval requests for shopping-item mutations
CREATE TABLE IF NOT EXISTS shopping_item_request (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES sb_group(id) ON DELETE CASCADE,
    requested_by_user_id BIGINT NOT NULL REFERENCES app_user(id),
    shopping_item_id BIGINT REFERENCES shopping_item(id) ON DELETE SET NULL,
    category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
    decided_by_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    item_version BIGINT,
    name VARCHAR(200) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL DEFAULT 1,
    unit VARCHAR(20),
    estimated_price NUMERIC(12,2),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    decided_at TIMESTAMP WITHOUT TIME ZONE,
    decision_note TEXT,
    CONSTRAINT chk_shopping_item_request_type CHECK (type IN ('ADD', 'EDIT', 'DELETE')),
    CONSTRAINT chk_shopping_item_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_shopping_item_request_quantity CHECK (quantity > 0),
    CONSTRAINT chk_shopping_item_request_price CHECK (estimated_price IS NULL OR estimated_price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_item_request_group_status
    ON shopping_item_request(group_id, status);
CREATE INDEX IF NOT EXISTS idx_item_request_requester
    ON shopping_item_request(requested_by_user_id, status);
CREATE INDEX IF NOT EXISTS idx_item_request_item
    ON shopping_item_request(shopping_item_id, status);
