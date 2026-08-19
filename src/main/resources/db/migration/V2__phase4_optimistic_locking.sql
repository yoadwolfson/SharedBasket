-- Phase 4 optimistic locking for categories and shopping items
ALTER TABLE category
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE shopping_item
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
