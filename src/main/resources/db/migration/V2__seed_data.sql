-- Seed Data for Restaurant ERP Backend

-- 1. Insert Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_CUSTOMER'),
(3, 'ROLE_KITCHEN'),
(4, 'ROLE_WAITER'),
(5, 'ROLE_CASHIER')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Insert Default Users (Password: password123)
-- BCrypt hash for 'password123': $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.i6u7H/7O
INSERT INTO users (id, username, email, mobile_number, password, full_name, enabled) VALUES
(1, 'admin', 'admin@restaurant.com', '+15550000001', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.i6u7H/7O', 'System Administrator', TRUE),
(2, 'customer1', 'customer1@gmail.com', '+15550000002', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.i6u7H/7O', 'John Doe Customer', TRUE),
(3, 'kitchen1', 'chef@restaurant.com', '+15550000003', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.i6u7H/7O', 'Chef Gordon Kitchen', TRUE),
(4, 'waiter1', 'waiter1@restaurant.com', '+15550000004', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.i6u7H/7O', 'Sam Waiter', TRUE),
(5, 'cashier1', 'cashier1@restaurant.com', '+15550000005', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.i6u7H/7O', 'Clara Cashier', TRUE)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 3. Map User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- customer1 -> ROLE_CUSTOMER
(3, 3), -- kitchen1 -> ROLE_KITCHEN
(4, 4), -- waiter1 -> ROLE_WAITER
(5, 5)  -- cashier1 -> ROLE_CASHIER
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- 4. Restaurant Tables
INSERT INTO restaurant_tables (id, table_number, capacity, status, location_section) VALUES
(1, 'T-01', 2, 'AVAILABLE', 'Main Dining Room'),
(2, 'T-02', 4, 'AVAILABLE', 'Main Dining Room'),
(3, 'T-03', 4, 'AVAILABLE', 'Window Section'),
(4, 'T-04', 6, 'AVAILABLE', 'Patio Deck'),
(5, 'T-05', 8, 'AVAILABLE', 'VIP Lounge')
ON DUPLICATE KEY UPDATE table_number=VALUES(table_number);

-- 5. Food Categories
INSERT INTO categories (id, name, description, image_url, active, display_order) VALUES
(1, 'Starters & Appetizers', 'Crispy and delicious appetizers to start your meal', 'https://images.unsplash.com/photo-1541529086526-db283c563270', TRUE, 1),
(2, 'Main Course', 'Hearty chef special main dishes and authentic curries', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c', TRUE, 2),
(3, 'Desserts & Sweets', 'Decadent cakes, ice creams, and sweet delights', 'https://images.unsplash.com/photo-1551024709-8f23befc6f87', TRUE, 3),
(4, 'Beverages & Drinks', 'Refreshing juices, mocktails, coffee, and teas', 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd', TRUE, 4)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 6. Menu Items
INSERT INTO menu_items (id, category_id, name, description, price, gst_percentage, prep_time_minutes, dietary_type, available, image_url) VALUES
(1, 1, 'Paneer Tikka Grill', 'Cottage cheese cubes marinated in spiced yogurt and grilled to perfection', 12.99, 5.00, 15, 'VEG', TRUE, 'https://images.unsplash.com/photo-1599487488170-d11ec9c172f0'),
(2, 1, 'Crispy Spring Rolls', 'Delicate pastry sheets stuffed with seasoned vegetables', 8.50, 5.00, 10, 'VEG', TRUE, 'https://images.unsplash.com/photo-1544025162-d76694265947'),
(3, 2, 'Butter Chicken Special', 'Tender chicken cooked in rich tomato butter sauce', 16.50, 5.00, 20, 'NON_VEG', TRUE, 'https://images.unsplash.com/photo-1588166524941-3bf61a9c41db'),
(4, 2, 'Shahi Paneer Curry', 'Rich cottage cheese in cashew cream curry gravy', 14.00, 5.00, 18, 'JAIN', TRUE, 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7'),
(5, 3, 'Sizzling Chocolate Brownie', 'Warm chocolate brownie topped with vanilla ice cream and hot fudge', 7.99, 5.00, 8, 'VEG', TRUE, 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c'),
(6, 4, 'Mango Mint Cooler', 'Fresh mango pulp blended with mint leaves and crushed ice', 4.99, 5.00, 5, 'VEG', TRUE, 'https://images.unsplash.com/photo-1534353473418-4cfa6c56fd38')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 7. Ingredients
INSERT INTO ingredients (id, name, unit_of_measure, current_stock, minimum_stock, unit_cost) VALUES
(1, 'Paneer (Cottage Cheese)', 'KG', 50.000, 10.000, 5.50),
(2, 'Chicken Breast', 'KG', 80.000, 15.000, 6.00),
(3, 'Butter & Cream', 'KG', 30.000, 5.000, 4.00),
(4, 'Tomatoes', 'KG', 100.000, 20.000, 1.50),
(5, 'Chocolate & Cocoa', 'KG', 25.000, 5.000, 8.00)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 8. Recipe Mapping
INSERT INTO menu_item_ingredients (id, menu_item_id, ingredient_id, quantity_required) VALUES
(1, 1, 1, 0.250), -- Paneer Tikka requires 0.25 KG Paneer
(2, 3, 2, 0.300), -- Butter Chicken requires 0.30 KG Chicken
(3, 3, 3, 0.100), -- Butter Chicken requires 0.10 KG Butter
(4, 3, 4, 0.200), -- Butter Chicken requires 0.20 KG Tomatoes
(5, 4, 1, 0.250), -- Shahi Paneer requires 0.25 KG Paneer
(6, 5, 5, 0.150)  -- Brownie requires 0.15 KG Chocolate
ON DUPLICATE KEY UPDATE quantity_required=VALUES(quantity_required);

-- 9. Coupons
INSERT INTO coupons (id, code, discount_percentage, max_discount_amount, min_order_amount, expiry_date, active) VALUES
(1, 'WELCOME10', 10.00, 100.00, 20.00, '2030-12-31 23:59:59', TRUE),
(2, 'FESTIVE20', 20.00, 200.00, 50.00, '2030-12-31 23:59:59', TRUE)
ON DUPLICATE KEY UPDATE code=VALUES(code);

-- 10. Settings
INSERT INTO restaurant_settings (setting_key, setting_value, description) VALUES
('RESTAURANT_NAME', 'Royal Gourmet Restaurant & Lounge', 'Name displayed on invoices and emails'),
('DEFAULT_GST_PERCENTAGE', '5.0', 'Default GST tax rate'),
('SERVICE_CHARGE_PERCENTAGE', '5.0', 'Default service charge rate'),
('CURRENCY_SYMBOL', '$', 'Currency symbol')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);
