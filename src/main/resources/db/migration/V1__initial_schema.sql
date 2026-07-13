-- Flyway initial schema for SharedBasket
-- Users
CREATE TABLE IF NOT EXISTS app_user (
	id BIGSERIAL PRIMARY KEY,
	username VARCHAR(50) NOT NULL UNIQUE,
	email VARCHAR(255) NOT NULL UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	first_name VARCHAR(100),
	last_name VARCHAR(100),
	enabled BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- Groups
CREATE TABLE IF NOT EXISTS sb_group (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(100) NOT NULL,
	invite_code VARCHAR(32) NOT NULL UNIQUE,
	owner_user_id BIGINT NOT NULL REFERENCES app_user(id),
	created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- Group members
CREATE TABLE IF NOT EXISTS group_member (
	id BIGSERIAL PRIMARY KEY,
	group_id BIGINT NOT NULL REFERENCES sb_group(id) ON DELETE CASCADE,
	user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
	role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
	joined_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
	UNIQUE (group_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_group_member_group_id ON group_member(group_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user_id ON group_member(user_id);

-- Categories
CREATE TABLE IF NOT EXISTS category (
	id BIGSERIAL PRIMARY KEY,
	group_id BIGINT NOT NULL REFERENCES sb_group(id) ON DELETE CASCADE,
	name VARCHAR(100) NOT NULL,
	color VARCHAR(20),
	sort_order INT NOT NULL DEFAULT 0,
	archived BOOLEAN NOT NULL DEFAULT FALSE,
	created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
	UNIQUE (group_id, name)
);

CREATE INDEX IF NOT EXISTS idx_category_group_id ON category(group_id);

-- Shopping items
CREATE TABLE IF NOT EXISTS shopping_item (
	id BIGSERIAL PRIMARY KEY,
	group_id BIGINT NOT NULL REFERENCES sb_group(id) ON DELETE CASCADE,
	category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
	name VARCHAR(200) NOT NULL,
	quantity NUMERIC(12,3) NOT NULL DEFAULT 1,
	unit VARCHAR(20),
	estimated_price NUMERIC(12,2),
	purchased BOOLEAN NOT NULL DEFAULT FALSE,
	purchased_at TIMESTAMP WITHOUT TIME ZONE,
	purchased_by_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
	notes TEXT,
	created_by_user_id BIGINT NOT NULL REFERENCES app_user(id),
	created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
	updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_shopping_item_group_id ON shopping_item(group_id);
CREATE INDEX IF NOT EXISTS idx_shopping_item_category_id ON shopping_item(category_id);
CREATE INDEX IF NOT EXISTS idx_shopping_item_purchased ON shopping_item(purchased);

-- Purchase history
CREATE TABLE IF NOT EXISTS purchase_record (
	id BIGSERIAL PRIMARY KEY,
	group_id BIGINT NOT NULL REFERENCES sb_group(id) ON DELETE CASCADE,
	shopping_item_id BIGINT REFERENCES shopping_item(id) ON DELETE SET NULL,
	category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
	item_name VARCHAR(200) NOT NULL,
	purchased_by_user_id BIGINT NOT NULL REFERENCES app_user(id),
	quantity NUMERIC(12,3) NOT NULL DEFAULT 1,
	unit VARCHAR(20),
	price_paid NUMERIC(12,2),
	purchased_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
	notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_purchase_record_group_id ON purchase_record(group_id);
CREATE INDEX IF NOT EXISTS idx_purchase_record_purchased_at ON purchase_record(purchased_at);
