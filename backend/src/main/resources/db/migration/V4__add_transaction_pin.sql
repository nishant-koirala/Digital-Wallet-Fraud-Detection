-- Nullable — same reasoning as phone_number originally, but see the
-- companion migration for why phone_number is now NOT NULL instead.
-- Schema-only for now — actual PIN-setting and verification logic
-- comes later, alongside JWT auth.
ALTER TABLE users
    ADD COLUMN pin_hash VARCHAR(255) NULL;