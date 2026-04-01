SET search_path TO analytics_derived;

-- =========================================================
-- Derived analytics fact tables
-- =========================================================

CREATE TABLE analytics_derived.bin_day_features
(
    bin_feature_snapshot_key            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_key                            INTEGER NOT NULL,
    bin_key                             BIGINT  NOT NULL,
    baseline_avg_visits_per_week_90d    NUMERIC(8, 4),
    baseline_avg_emptyings_per_week_90d NUMERIC(8, 4),
    low_fill_visit_ratio_90d            NUMERIC(8, 4),
    not_emptied_ratio_90d               NUMERIC(8, 4),
    emptying_rank_90d                   INTEGER,
    weather_sensitivity_score           NUMERIC(8, 4),
    rain_sensitivity_score              NUMERIC(8, 4),
    sun_sensitivity_score               NUMERIC(8, 4),
    heat_sensitivity_score              NUMERIC(8, 4),
    event_sensitivity_score             NUMERIC(8, 4),
    days_since_last_visit               INTEGER CHECK (days_since_last_visit IS NULL OR days_since_last_visit >= 0),
    days_since_last_emptying            INTEGER CHECK (days_since_last_emptying IS NULL OR days_since_last_emptying >= 0),

    CONSTRAINT fk_bin_day_features_date
        FOREIGN KEY (date_key) REFERENCES analytics.dim_date (date_key),
    CONSTRAINT fk_bin_day_features_bin
        FOREIGN KEY (bin_key) REFERENCES analytics.dim_bin (bin_key),

    CONSTRAINT uq_bin_day_features UNIQUE (date_key, bin_key)
);

CREATE TABLE analytics_derived.system_day_summary
(
    system_day_key             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_key                   INTEGER       NOT NULL UNIQUE,
    active_bin_count           INTEGER       NOT NULL CHECK (active_bin_count >= 0),
    visited_distinct_bin_count INTEGER       NOT NULL CHECK (visited_distinct_bin_count >= 0),
    emptied_distinct_bin_count INTEGER       NOT NULL CHECK (emptied_distinct_bin_count >= 0),
    bin_visit_count            INTEGER       NOT NULL CHECK (bin_visit_count >= 0),
    emptied_visit_count        INTEGER       NOT NULL CHECK (emptied_visit_count >= 0),
    vehicle_emptying_count     INTEGER       NOT NULL CHECK (vehicle_emptying_count >= 0),
    low_fill_visit_count       INTEGER       NOT NULL CHECK (low_fill_visit_count >= 0),
    low_fill_emptied_count     INTEGER       NOT NULL CHECK (low_fill_emptied_count >= 0),
    high_fill_visit_count      INTEGER       NOT NULL CHECK (high_fill_visit_count >= 0),
    high_fill_emptied_count    INTEGER       NOT NULL CHECK (high_fill_emptied_count >= 0),
    overfull_visit_count       INTEGER       NOT NULL CHECK (overfull_visit_count >= 0),
    overfull_emptied_count     INTEGER       NOT NULL CHECK (overfull_emptied_count >= 0),
    bin_visit_count_7d         INTEGER       NOT NULL CHECK (bin_visit_count_7d >= 0),
    emptied_visit_count_7d     INTEGER       NOT NULL CHECK (emptied_visit_count_7d >= 0),
    visit_emptied_ratio_7d     NUMERIC(8, 4) NOT NULL,
    low_fill_visit_ratio_90d   NUMERIC(8, 4) NOT NULL,
    low_fill_emptied_ratio_90d NUMERIC(8, 4) NOT NULL,
    overfull_visit_30d         INTEGER       NOT NULL CHECK (overfull_visit_30d >= 0),

    CONSTRAINT fk_system_day_summary_date
        FOREIGN KEY (date_key) REFERENCES analytics.dim_date (date_key)
);


-- =========================================================
-- indexes
-- =========================================================

CREATE INDEX idx_bin_day_features_date_key
    ON analytics_derived.bin_day_features (date_key);

CREATE INDEX idx_bin_day_features_bin_key
    ON analytics_derived.bin_day_features (bin_key);

CREATE INDEX idx_system_day_summary_date_key
    ON analytics_derived.system_day_summary (date_key);
