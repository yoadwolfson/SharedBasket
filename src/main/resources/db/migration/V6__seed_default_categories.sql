-- Seed the pre-integrated categories into every existing group (idempotent)
INSERT INTO category (group_id, name, color, emoji, archived, version, created_at, updated_at)
SELECT g.id, d.name, d.color, d.emoji, FALSE, 0, now(), now()
FROM sb_group g
CROSS JOIN (VALUES
    ('Dairy', '#3498db', '🥛'),
    ('Meat & Fish', '#e74c3c', '🥩'),
    ('Fruits & Vegetables', '#27ae60', '🥬'),
    ('Bakery', '#f39c12', '🥖'),
    ('Frozen', '#16a085', '🧊'),
    ('Pantry', '#8e44ad', '🥫'),
    ('Beverages', '#3498db', '🧃'),
    ('Snacks', '#f39c12', '🍪'),
    ('Household', '#7f8c8d', '🧻'),
    ('Personal Care', '#8e44ad', '🧴'),
    ('Other', '#7f8c8d', '🛒')
) AS d(name, color, emoji)
WHERE NOT EXISTS (
    SELECT 1 FROM category c
    WHERE c.group_id = g.id AND lower(c.name) = lower(d.name)
);
