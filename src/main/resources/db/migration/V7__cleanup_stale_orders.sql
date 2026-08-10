-- Clean up old stale test orders that were never completed
-- Keep only orders from the last 24 hours, close everything older
UPDATE orders SET status = 'CLOSED', closed_at = NOW()
WHERE status IN ('NEW', 'RECEIVED', 'PREPARING', 'READY', 'DELIVERED')
AND placed_at < NOW() - INTERVAL 1 HOUR;
