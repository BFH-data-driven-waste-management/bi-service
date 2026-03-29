INSERT INTO analytics.dim_fill_level (fill_level_key, fill_level_code, ordinal_rank, is_low, is_high, is_overfull) VALUES (1, 'EMPTY_OR_ALMOST_EMPTY', 1, true, false, false);
INSERT INTO analytics.dim_fill_level (fill_level_key, fill_level_code, ordinal_rank, is_low, is_high, is_overfull) VALUES (2, 'HALF_FULL', 2, false, false, false);
INSERT INTO analytics.dim_fill_level (fill_level_key, fill_level_code, ordinal_rank, is_low, is_high, is_overfull) VALUES (3, 'FULL', 3, false, true, false);
INSERT INTO analytics.dim_fill_level (fill_level_key, fill_level_code, ordinal_rank, is_low, is_high, is_overfull) VALUES (4, 'OVERFULL', 4, false, true, true);
