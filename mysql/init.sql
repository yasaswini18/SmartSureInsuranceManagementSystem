-- Creates all databases when MySQL container starts
-- This runs automatically on first startup

CREATE DATABASE IF NOT EXISTS authdb;
CREATE DATABASE IF NOT EXISTS policydb;
CREATE DATABASE IF NOT EXISTS claimsdb;
CREATE DATABASE IF NOT EXISTS admindb;

-- Grant all privileges to root user for all databases
GRANT ALL PRIVILEGES ON authdb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON policydb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON claimsdb.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON admindb.* TO 'root'@'%';

FLUSH PRIVILEGES;