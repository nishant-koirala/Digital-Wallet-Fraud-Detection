CREATE INDEX idx_transactions_from_wallet_created_at ON transactions (from_wallet_id, created_at);

ALTER TABLE users ADD COLUMN fraud_risk_score INT NOT NULL DEFAULT 0;
