-- V3 Ecosystem Upgrade

-- 1. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    table_id BIGINT,
    party_size INT NOT NULL,
    reservation_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(id)
);

-- 2. Add a "Setup Complete" flag to settings if it doesn't exist
INSERT INTO restaurant_settings (setting_key, setting_value, description)
VALUES ('IS_SETUP_COMPLETE', 'true', 'Indicates if the first-time setup is complete')
ON DUPLICATE KEY UPDATE setting_value = 'true';

