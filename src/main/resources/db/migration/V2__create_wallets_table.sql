-- Create wallets table
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    wallet_number VARCHAR(20) NOT NULL UNIQUE,
    balance NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    daily_limit NUMERIC(19,2) DEFAULT 10000.00,
    transaction_limit NUMERIC(19,2) DEFAULT 5000.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Add foreign key constraint
ALTER TABLE wallets
ADD CONSTRAINT fk_wallet_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE CASCADE;

-- Add check constraints
ALTER TABLE wallets
ADD CONSTRAINT chk_wallet_status
CHECK (status IN ('ACTIVE', 'FROZEN', 'SUSPENDED', 'CLOSED'));

ALTER TABLE wallets
ADD CONSTRAINT chk_balance_positive
CHECK (balance >= 0);

ALTER TABLE wallets
ADD CONSTRAINT chk_limits_positive
CHECK (daily_limit >= 0 AND transaction_limit >= 0);

-- Create indexes for faster queries
CREATE INDEX idx_user_id ON wallets(user_id);
CREATE INDEX idx_wallet_number ON wallets(wallet_number);
CREATE INDEX idx_wallet_status ON wallets(status);

-- Add comments
COMMENT ON TABLE wallets IS 'Digital wallets for users';
COMMENT ON COLUMN wallets.wallet_number IS 'Unique wallet identifier for transfers';
COMMENT ON COLUMN wallets.version IS 'Optimistic locking version for concurrent updates';