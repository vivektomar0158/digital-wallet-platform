-- Migration V4: Fix transaction type constraint and add missing wallet columns
-- Author: System
-- Date: 2026-01-06
-- Purpose: Fix constraint violation and schema inconsistencies
-- ============================================================================
-- PART 1: Fix Transaction Type Constraint
-- ============================================================================
-- Drop the old constraint that expects 'P2P_TRANSFER'
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS chk_transaction_type;
-- Add new constraint that matches Java enum (uses 'TRANSFER' instead of 'P2P_TRANSFER')
ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_type CHECK (
        type IN (
            'TRANSFER',
            'DEPOSIT',
            'WITHDRAWAL',
            'REFUND',
            'PAYMENT',
            'CASHBACK',
            'FEE'
        )
    );
-- ============================================================================
-- PART 2: Add Missing Wallet Columns for Spending Limits
-- ============================================================================
-- Add total_daily_spent column if it doesn't exist
ALTER TABLE wallets
ADD COLUMN IF NOT EXISTS total_daily_spent NUMERIC(19, 2) DEFAULT 0.00;
-- Add total_monthly_spent column if it doesn't exist
ALTER TABLE wallets
ADD COLUMN IF NOT EXISTS total_monthly_spent NUMERIC(19, 2) DEFAULT 0.00;
-- Add check constraint to ensure spending amounts are non-negative
ALTER TABLE wallets DROP CONSTRAINT IF EXISTS chk_spending_positive;
ALTER TABLE wallets
ADD CONSTRAINT chk_spending_positive CHECK (
        total_daily_spent >= 0
        AND total_monthly_spent >= 0
    );
-- ============================================================================
-- PART 3: Fix Wallet Number Length Inconsistency
-- ============================================================================
-- Update wallet_number column length from VARCHAR(20) to VARCHAR(30)
ALTER TABLE wallets
ALTER COLUMN wallet_number TYPE VARCHAR(30);
-- ============================================================================
-- PART 4: Add Indexes for Better Performance
-- ============================================================================
-- Index for querying wallets by spending limits (useful for limit checks)
CREATE INDEX IF NOT EXISTS idx_wallet_spending ON wallets(total_daily_spent, total_monthly_spent);
-- ============================================================================
-- PART 5: Add Comments for Documentation
-- ============================================================================
COMMENT ON COLUMN wallets.total_daily_spent IS 'Total amount spent today for daily limit tracking';
COMMENT ON COLUMN wallets.total_monthly_spent IS 'Total amount spent this month for monthly limit tracking';
COMMENT ON CONSTRAINT chk_transaction_type ON transactions IS 'Ensures transaction type matches Java TransactionType enum';
