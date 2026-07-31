-- Restaurant ERP V1 Initial Database Schema

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile_number VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    account_non_locked BOOLEAN DEFAULT TRUE,
    failed_attempt INT DEFAULT 0,
    lock_time DATETIME,
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. User Roles Mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Login History
CREATE TABLE IF NOT EXISTS login_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50) NOT NULL,
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255)
);

-- 6. Categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 7. Menu Items
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    gst_percentage DECIMAL(5, 2) DEFAULT 5.00,
    prep_time_minutes INT DEFAULT 15,
    dietary_type VARCHAR(20) NOT NULL DEFAULT 'VEG',
    available BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(500),
    deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_items_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 8. Restaurant Tables
CREATE TABLE IF NOT EXISTS restaurant_tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number VARCHAR(20) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    location_section VARCHAR(50) DEFAULT 'Main Dining',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 9. Carts
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    table_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_carts_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(id) ON DELETE SET NULL
);

-- 10. Cart Items
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    special_instructions VARCHAR(255),
    unit_price DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- 11. Orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    table_id BIGINT,
    waiter_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    order_type VARCHAR(20) NOT NULL DEFAULT 'DINE_IN',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    remarks VARCHAR(255),
    placed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at DATETIME,
    closed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_orders_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(id),
    CONSTRAINT fk_orders_waiter FOREIGN KEY (waiter_id) REFERENCES users(id)
);

-- 12. Order Items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    special_instructions VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- 13. Coupons
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    discount_percentage DECIMAL(5, 2) NOT NULL,
    max_discount_amount DECIMAL(10, 2) DEFAULT 500.00,
    min_order_amount DECIMAL(10, 2) DEFAULT 0.00,
    expiry_date DATETIME NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- 14. Bills
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    cashier_id BIGINT,
    item_total DECIMAL(10, 2) NOT NULL,
    gst_amount DECIMAL(10, 2) NOT NULL,
    service_charge DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    coupon_code VARCHAR(30),
    round_off DECIMAL(5, 2) DEFAULT 0.00,
    grand_total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    pdf_file_path VARCHAR(500),
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bills_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_bills_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_bills_cashier FOREIGN KEY (cashier_id) REFERENCES users(id)
);

-- 15. Payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    cashier_id BIGINT,
    payment_method VARCHAR(30) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    reference_number VARCHAR(100),
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_bill FOREIGN KEY (bill_id) REFERENCES bills(id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_payments_cashier FOREIGN KEY (cashier_id) REFERENCES users(id)
);

-- 16. Ingredients
CREATE TABLE IF NOT EXISTS ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    unit_of_measure VARCHAR(20) NOT NULL,
    current_stock DECIMAL(10, 3) NOT NULL DEFAULT 0.000,
    minimum_stock DECIMAL(10, 3) NOT NULL DEFAULT 10.000,
    unit_cost DECIMAL(10, 2) DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 17. Suppliers
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 18. Menu Item Ingredients Recipe Mapping
CREATE TABLE IF NOT EXISTS menu_item_ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity_required DECIMAL(10, 3) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipe_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE,
    CONSTRAINT uk_menu_item_ingredient UNIQUE (menu_item_id, ingredient_id)
);

-- 19. Inventory Logs
CREATE TABLE IF NOT EXISTS inventory_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id BIGINT NOT NULL,
    quantity_change DECIMAL(10, 3) NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    reference_id VARCHAR(100),
    performed_by VARCHAR(100),
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255),
    CONSTRAINT fk_inventory_logs_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE
);

-- 20. Employees
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    employee_code VARCHAR(30) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    designation VARCHAR(50) NOT NULL,
    base_salary DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    joining_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 21. Employee Shifts
CREATE TABLE IF NOT EXISTS employee_shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    shift_type VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    notes VARCHAR(255),
    CONSTRAINT fk_employee_shifts_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- 22. Employee Attendance
CREATE TABLE IF NOT EXISTS employee_attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time DATETIME,
    check_out_time DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    CONSTRAINT fk_employee_attendance_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uk_emp_date UNIQUE (employee_id, attendance_date)
);

-- 23. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    user_role VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(50),
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 24. Restaurant Settings
CREATE TABLE IF NOT EXISTS restaurant_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
