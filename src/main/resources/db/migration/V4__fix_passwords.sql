-- Fix password hashes for default users to 'password123'
UPDATE users SET password = '$2a$10$V.v6gHBIF9IhjhmceMtUr.0m.cOZt/qalD0eyLp6q38zDdUkLXgJ.', failed_attempt = 0, account_non_locked = 1;
