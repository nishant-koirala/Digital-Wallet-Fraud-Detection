-- Nullable — not every request will have granted browser location
-- permission, and older transactions (before this feature existed)
-- won't have any location data at all.
ALTER TABLE transactions
    ADD COLUMN latitude DECIMAL(9,6) NULL,
    ADD COLUMN longitude DECIMAL(9,6) NULL;