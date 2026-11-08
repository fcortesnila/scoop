INSERT INTO flavors (id, name, price, available) VALUES
(1, 'Vanilla Bean', 4.00, TRUE), (2, 'Chocolate Fudge', 4.50, TRUE),
(3, 'Strawberry Fields', 4.50, TRUE), (4, 'Mint Chip', 4.75, TRUE),
(5, 'Salted Caramel', 5.00, TRUE), (6, 'Cookie Dough', 5.00, TRUE),
(7, 'Mango Sorbet', 4.25, TRUE), (8, 'Pistachio Dream', 5.25, TRUE);

INSERT INTO ingredients (id, name, unit, stock_qty, low_stock_threshold) VALUES
(1, 'Vanilla base', 'L', 80, 12), (2, 'Chocolate base', 'L', 65, 12),
(3, 'Strawberry base', 'L', 55, 10), (4, 'Mint base', 'L', 50, 10),
(5, 'Caramel base', 'L', 48, 10), (6, 'Cookie dough', 'kg', 24, 5),
(7, 'Mango puree', 'L', 35, 8), (8, 'Pistachio base', 'L', 30, 7),
(9, 'Chocolate chips', 'kg', 18, 4), (10, 'Caramel swirl', 'L', 16, 4),
(11, 'Cookie pieces', 'kg', 14, 3), (12, 'Pistachios', 'kg', 8, 2);

INSERT INTO flavor_ingredients (flavor_id, ingredient_id, qty_per_serving) VALUES
(1, 1, 0.12), (2, 2, 0.12), (2, 9, 0.015), (3, 3, 0.12),
(4, 4, 0.12), (4, 9, 0.02), (5, 1, 0.11), (5, 10, 0.025),
(6, 1, 0.10), (6, 6, 0.035), (6, 11, 0.015), (7, 7, 0.13),
(8, 8, 0.12), (8, 12, 0.02);

INSERT INTO stops (id, name, latitude, longitude, started_at, ended_at) VALUES
(1, 'Riverside Park', 40.7138000, -74.0060000, DATE_SUB(NOW(6), INTERVAL 21 DAY), DATE_ADD(DATE_SUB(NOW(6), INTERVAL 21 DAY), INTERVAL 3 HOUR)),
(2, 'Maple Street Market', 40.7192000, -74.0021000, DATE_SUB(NOW(6), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(NOW(6), INTERVAL 20 DAY), INTERVAL 4 HOUR)),
(3, 'Community Green', 40.7251000, -74.0112000, DATE_SUB(NOW(6), INTERVAL 19 DAY), DATE_ADD(DATE_SUB(NOW(6), INTERVAL 19 DAY), INTERVAL 3 HOUR)),
(4, 'Riverside Park', 40.7138000, -74.0060000, DATE_SUB(NOW(6), INTERVAL 14 DAY), DATE_ADD(DATE_SUB(NOW(6), INTERVAL 14 DAY), INTERVAL 4 HOUR)),
(5, 'Maple Street Market', 40.7192000, -74.0021000, DATE_SUB(NOW(6), INTERVAL 7 DAY), DATE_ADD(DATE_SUB(NOW(6), INTERVAL 7 DAY), INTERVAL 4 HOUR)),
(6, 'Community Green', 40.7251000, -74.0112000, DATE_SUB(NOW(6), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(6), INTERVAL 3 DAY), INTERVAL 3 HOUR));

INSERT INTO orders (user_id, stop_id, status, total, created_at)
SELECT NULL, s.id, 'SERVED', 8 + MOD(numbers.seq * 7, 23),
       DATE_ADD(s.started_at, INTERVAL MOD(numbers.seq, 3) HOUR) + INTERVAL MOD(numbers.seq * 7, 60) MINUTE
FROM (
    SELECT ones.n + tens.n * 10 + 1 AS seq
    FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
    CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
) numbers
JOIN stops s ON s.id = 1 + MOD(FLOOR((numbers.seq - 1) / 15), 6)
WHERE numbers.seq <= 90;

INSERT INTO order_items (order_id, flavor_id, quantity, unit_price)
SELECT o.id, 1 + MOD(o.id, 8), 1 + MOD(o.id, 3), 4 + MOD(o.id, 6) * 0.25
FROM orders o
WHERE o.id > 0;
