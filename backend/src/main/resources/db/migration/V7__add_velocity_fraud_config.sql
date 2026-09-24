ALTER TABLE fraud_config
ADD COLUMN velocity_window_minutes INT NOT NULL DEFAULT 10,
ADD COLUMN velocity_lookback_windows INT NOT NULL DEFAULT 6,
ADD COLUMN velocity_cold_start_max INT NOT NULL DEFAULT 5,
ADD COLUMN velocity_multiplier DOUBLE NOT NULL DEFAULT 3.0;
