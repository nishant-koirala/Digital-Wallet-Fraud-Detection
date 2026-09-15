CREATE TABLE fraud_config (
    id INT PRIMARY KEY,
    cold_start_threshold DECIMAL(15, 4) NOT NULL,
    min_history_for_baseline INT NOT NULL,
    average_multiplier DECIMAL(15, 4) NOT NULL,
    max_geo_distance_km DOUBLE NOT NULL
);

INSERT INTO fraud_config (id, cold_start_threshold, min_history_for_baseline, average_multiplier, max_geo_distance_km)
VALUES (1, 50000.0000, 5, 5.0000, 500.0);
