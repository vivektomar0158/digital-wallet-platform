-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    from_wallet_id BIGINT,
    to_wallet_id BIGINT,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reference_id VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    metadata VARCHAR(500),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);
-- Add foreign key constraints
ALTER TABLE transactions
ADD CONSTRAINT fk_from_wallet FOREIGN KEY (from_wallet_id) REFERENCES wallets(id);
ALTER TABLE transactions
ADD CONSTRAINT fk_to_wallet FOREIGN KEY (to_wallet_id) REFERENCES wallets(id);
-- Add check constraints
ALTER TABLE transactions
ADD CONSTRAINT chk_amount_positive CHECK (amount > 0);
ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_type CHECK (
        type IN (
            'P2P_TRANSFER',
            'DEPOSIT',
            'WITHDRAWAL',
            'REFUND',
            'PAYMENT',
            'CASHBACK',
            'FEE'
        )
    );
ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_status CHECK (
        status IN (
            'PENDING',
            'PROCESSING',
            'COMPLETED',
            'FAILED',
            'CANCELLED',
            'REFUNDED'
        )
    );
ALTER TABLE transactions
ADD CONSTRAINT chk_wallets_different CHECK (
        from_wallet_id != to_wallet_id
        OR from_wallet_id IS NULL
        OR to_wallet_id IS NULL
    );
-- Create indexes for faster queries
CREATE INDEX idx_from_wallet ON transactions(from_wallet_id);
CREATE INDEX idx_to_wallet ON transactions(to_wallet_id);
CREATE INDEX idx_reference_id ON transactions(reference_id);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_created_at ON transactions(created_at DESC);
CREATE INDEX idx_type_status ON transactions(type, status);
-- Add comments
COMMENT ON TABLE transactions IS 'All wallet transactions and transfers';
COMMENT ON COLUMN transactions.reference_id IS 'Unique transaction reference for idempotency';
COMMENT ON COLUMN transactions.metadata IS 'JSON string for additional transaction data';