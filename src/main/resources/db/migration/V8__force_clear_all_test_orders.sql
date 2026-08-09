-- Force wipe all active test orders from the database to reset the environment for final testing
UPDATE orders SET status = 'CLOSED', closed_at = NOW()
WHERE status NOT IN ('CLOSED', 'PAID', 'CANCELLED');
