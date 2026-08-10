-- Delete dummy users created during initial setup to give the owner a clean slate
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username IN ('EMP-0001', 'EMP-0002', 'EMP-0003', 'testwaiter', 'testkitchen'));
DELETE FROM employees WHERE user_id IN (SELECT id FROM users WHERE username IN ('EMP-0001', 'EMP-0002', 'EMP-0003', 'testwaiter', 'testkitchen'));
DELETE FROM users WHERE username IN ('EMP-0001', 'EMP-0002', 'EMP-0003', 'testwaiter', 'testkitchen');
