-- V5: Update usernames to EMP-ID format for staff login
-- owner/admin stays as 'owner'
UPDATE users SET username = 'EMP-0001' WHERE id = 3; -- kitchen1 -> EMP-0001
UPDATE users SET username = 'EMP-0002' WHERE id = 4; -- waiter1  -> EMP-0002
UPDATE users SET username = 'EMP-0003' WHERE id = 5; -- cashier1 -> EMP-0003
-- customer1 stays as 'customer1'
