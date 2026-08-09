-- V6: Add assigned_waiter column to restaurant_tables for cross-device waiter assignment sync
ALTER TABLE restaurant_tables ADD COLUMN IF NOT EXISTS assigned_waiter VARCHAR(50) NULL;
