-- Normalize shopping item units onto the ItemUnit enum (PIECE / KILOGRAM)
UPDATE shopping_item
SET unit = 'KILOGRAM'
WHERE lower(coalesce(unit, '')) IN ('kg', 'kilo', 'kilogram', 'kilograms');

UPDATE shopping_item
SET unit = 'PIECE'
WHERE unit IS NULL OR unit <> 'KILOGRAM';

ALTER TABLE shopping_item ALTER COLUMN unit SET DEFAULT 'PIECE';
ALTER TABLE shopping_item ALTER COLUMN unit SET NOT NULL;

UPDATE shopping_item_request
SET unit = 'KILOGRAM'
WHERE lower(coalesce(unit, '')) IN ('kg', 'kilo', 'kilogram', 'kilograms');

UPDATE shopping_item_request
SET unit = 'PIECE'
WHERE unit IS NULL OR unit <> 'KILOGRAM';

ALTER TABLE shopping_item_request ALTER COLUMN unit SET DEFAULT 'PIECE';
ALTER TABLE shopping_item_request ALTER COLUMN unit SET NOT NULL;
