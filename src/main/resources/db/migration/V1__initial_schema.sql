-- Flyway initial schema for SharedBasket
-- Users
CREATE TABLE IF NOT EXISTS app_user (
	id BIGSERIAL PRIMARY KEY,
	username VARCHAR(100) NOT NULL UNIQUE,
	email VARCHAR(255) NOT NULL UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	display_name VARCHAR(255),
	created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
	updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

-- Groups
CREATE TABLE IF NOT EXISTS sb_group (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	invitation_code VARCHAR(100),
	created_by BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
	created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

-- Group members
CREATE TABLE IF NOT EXISTS group_member (
	id BIGSERIAL PRIMARY KEY,
	group_id BIGINT NOT NULL REFERENCES sb_group(id) ON DELETE CASCADE,
	user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
	joined_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
	UNIQUE (group_id, user_id)
);

-- Categories
CREATE TABLE IF NOT EXISTS category (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	owner_group_id BIGINT REFERENCES sb_group(id) ON DELETE SET NULL
);

-- Shopping items
CREATE TABLE IF NOT EXISTS shopping_item (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	quantity INTEGER DEFAULT 1,
	price NUMERIC(12,2) DEFAULT 0.00,
	purchased BOOLEAN DEFAULT false,
	category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
	created_by BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
	updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

-- Purchase history
CREATE TABLE IF NOT EXISTS purchase_record (
	id BIGSERIAL PRIMARY KEY,
	shopping_item_id BIGINT REFERENCES shopping_item(id) ON DELETE SET NULL,
	purchased_by BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
	quantity INTEGER NOT NULL DEFAULT 1,
	price NUMERIC(12,2) NOT NULL DEFAULT 0.00,
	purchased_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

-- Example indexes
CREATE INDEX IF NOT EXISTS idx_shopping_item_category ON shopping_item(category_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user ON group_member(user_id);
