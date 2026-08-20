-- Categories carry an emoji; display order is no longer a product concept
ALTER TABLE category ADD COLUMN IF NOT EXISTS emoji VARCHAR(8);
ALTER TABLE category DROP COLUMN IF EXISTS sort_order;
