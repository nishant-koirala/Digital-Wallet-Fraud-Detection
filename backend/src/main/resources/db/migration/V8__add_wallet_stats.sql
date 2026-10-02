ALTER TABLE wallets
ADD COLUMN total_transaction_volume DECIMAL(19,4) DEFAULT 0.0000,
ADD COLUMN transaction_count BIGINT DEFAULT 0;

-- Backfill stats from existing completed transactions
UPDATE wallets w
SET w.total_transaction_volume = (
    SELECT COALESCE(SUM(t.amount), 0.0000)
    FROM transactions t
    WHERE t.from_wallet_id = w.id AND t.status = 'COMPLETED'
),
w.transaction_count = (
    SELECT COUNT(*)
    FROM transactions t
    WHERE t.from_wallet_id = w.id AND t.status = 'COMPLETED'
);
