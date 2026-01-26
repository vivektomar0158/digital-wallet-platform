-- V6: Drop the Hibernate auto-generated constraint that conflicts with Flyway's constraint
-- The constraint 'transactions_type_check' was created by Hibernate ddl-auto and has stale enum values

ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_type_check;
