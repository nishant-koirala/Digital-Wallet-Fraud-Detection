-- Since test data is disposable, this migration assumes the users
-- table is empty (or only contains rows you're fine losing) at the
-- time it runs — NOT NULL with no default will fail on a table that
-- already has rows without a phone number.
ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(20) NOT NULL,
    ADD UNIQUE KEY uq_users_phone_number (phone_number);