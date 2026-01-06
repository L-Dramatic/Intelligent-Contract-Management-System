USE contract_system;
UPDATE sys_user SET role = 'COUNTY' WHERE id = 83 AND username = 'xxy';
SELECT id, username, role FROM sys_user WHERE id = 83;

