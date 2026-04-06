SET search_path TO analytics;

-- =========================================================
-- Dimension tables
-- =========================================================

CREATE TABLE dim_date
(
    date_key     INTEGER PRIMARY KEY,
    full_date    DATE        NOT NULL UNIQUE,
    year         SMALLINT    NOT NULL,
    quarter      SMALLINT    NOT NULL CHECK (quarter BETWEEN 1 AND 4),
    month        SMALLINT    NOT NULL CHECK (month BETWEEN 1 AND 12),
    month_name   VARCHAR(20) NOT NULL,
    week_of_year SMALLINT    NOT NULL CHECK (week_of_year BETWEEN 1 AND 53),
    day_of_month SMALLINT    NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    day_of_week  SMALLINT    NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    day_name     VARCHAR(20) NOT NULL,
    is_weekend   BOOLEAN     NOT NULL
);

CREATE TABLE dim_zone
(
    zone_key  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zone_name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_dim_zone UNIQUE (zone_name)
);

CREATE TABLE dim_bin
(
    bin_key       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bin_id        BIGINT         NOT NULL UNIQUE,
    bin_type      VARCHAR(128)   NOT NULL,
    volume_liters INTEGER        NOT NULL CHECK (volume_liters > 0),
    coord_x_2056  NUMERIC(12, 2) NOT NULL,
    coord_y_2056  NUMERIC(12, 2) NOT NULL,
    coord_x_4326  NUMERIC(9, 6)  NOT NULL,
    coord_y_4326  NUMERIC(9, 6)  NOT NULL,
    CONSTRAINT chk_dim_bin_latitude
        CHECK (coord_x_4326 BETWEEN -90 AND 90),
    CONSTRAINT chk_dim_bin_longitude
        CHECK (coord_y_4326 BETWEEN -180 AND 180),
    CONSTRAINT uq_dim_bin_coord_2056 UNIQUE (coord_x_2056, coord_y_2056),
    CONSTRAINT uq_dim_bin_coord_4326 UNIQUE (coord_x_4326, coord_y_4326)
);

CREATE TABLE dim_vehicle
(
    vehicle_key   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id    BIGINT      NOT NULL UNIQUE,
    license_plate VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE dim_fill_level
(
    fill_level_key  SMALLINT PRIMARY KEY,
    fill_level_code VARCHAR(32) NOT NULL UNIQUE,
    ordinal_rank    SMALLINT    NOT NULL UNIQUE,
    is_low          BOOLEAN     NOT NULL,
    is_high         BOOLEAN     NOT NULL,
    is_overfull     BOOLEAN     NOT NULL
);

CREATE TABLE dim_action
(
    action_key   SMALLINT PRIMARY KEY,
    action_code  VARCHAR(32) NOT NULL UNIQUE,
    emptied_flag BOOLEAN     NOT NULL
);

CREATE TABLE dim_connectivity_state
(
    connectivity_state_key  SMALLINT PRIMARY KEY,
    connectivity_state_code VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE dim_event
(
    event_key               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_name              VARCHAR(200) NOT NULL,
    expected_people_per_day INTEGER CHECK (expected_people_per_day IS NULL OR expected_people_per_day >= 0),
    start_date_key          INTEGER      NOT NULL,
    end_date_key            INTEGER      NOT NULL,
    CONSTRAINT fk_dim_event_start_date
        FOREIGN KEY (start_date_key) REFERENCES dim_date (date_key),
    CONSTRAINT fk_dim_event_end_date
        FOREIGN KEY (end_date_key) REFERENCES dim_date (date_key),
    CONSTRAINT chk_dim_event_date_range
        CHECK (
            start_date_key IS NULL
                OR end_date_key IS NULL
                OR start_date_key <= end_date_key
            )
);

-- =========================================================
-- Fact tables
-- =========================================================

CREATE TABLE fact_tour
(
    tour_key                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tour_id                 BIGINT      NOT NULL UNIQUE,
    vehicle_key             BIGINT      NOT NULL,
    date_key                INTEGER     NOT NULL,
    started_at_ts           TIMESTAMPTZ NOT NULL,
    ended_at_ts             TIMESTAMPTZ NOT NULL CHECK (ended_at_ts >= started_at_ts),
    visit_count             INTEGER     NOT NULL CHECK (visit_count >= 0),
    emptied_visit_count     INTEGER     NOT NULL CHECK (emptied_visit_count >= 0),
    not_emptied_visit_count INTEGER     NOT NULL CHECK (not_emptied_visit_count >= 0),
    low_fill_visit_count    INTEGER     NOT NULL DEFAULT 0 CHECK (low_fill_visit_count >= 0),
    high_fill_visit_count   INTEGER     NOT NULL DEFAULT 0 CHECK (high_fill_visit_count >= 0),
    overfull_visit_count    INTEGER     NOT NULL DEFAULT 0 CHECK (overfull_visit_count >= 0),
    vehicle_emptying_count  INTEGER     NOT NULL CHECK (vehicle_emptying_count >= 0),

    CONSTRAINT fk_fact_tour_vehicle
        FOREIGN KEY (vehicle_key) REFERENCES dim_vehicle (vehicle_key),

    CONSTRAINT fk_fact_tour_date
        FOREIGN KEY (date_key) REFERENCES dim_date (date_key)
);

CREATE TABLE fact_bin_visit
(
    bin_visit_key           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bin_visit_id            BIGINT      NOT NULL UNIQUE,
    tour_id                 BIGINT      NOT NULL,
    sequence_in_tour        INTEGER     NOT NULL CHECK (sequence_in_tour >= 1),
    event_ts                TIMESTAMPTZ NOT NULL,
    date_key                INTEGER     NOT NULL,
    bin_key                 BIGINT      NOT NULL,
    vehicle_key             BIGINT      NOT NULL,
    fill_level_key          SMALLINT    NOT NULL,
    action_key              SMALLINT    NOT NULL,
    connectivity_state_key  SMALLINT    NOT NULL,
    visit_count             SMALLINT    NOT NULL DEFAULT 1 CHECK (visit_count >= 0),
    emptied_visit_count     SMALLINT    NOT NULL CHECK (emptied_visit_count >= 0),
    not_emptied_visit_count SMALLINT    NOT NULL CHECK (not_emptied_visit_count >= 0),

    CONSTRAINT fk_fact_bin_visit_date
        FOREIGN KEY (date_key) REFERENCES dim_date (date_key),
    CONSTRAINT fk_fact_bin_visit_bin
        FOREIGN KEY (bin_key) REFERENCES dim_bin (bin_key),
    CONSTRAINT fk_fact_bin_visit_vehicle
        FOREIGN KEY (vehicle_key) REFERENCES dim_vehicle (vehicle_key),
    CONSTRAINT fk_fact_bin_visit_fill_level
        FOREIGN KEY (fill_level_key) REFERENCES dim_fill_level (fill_level_key),
    CONSTRAINT fk_fact_bin_visit_action
        FOREIGN KEY (action_key) REFERENCES dim_action (action_key),
    CONSTRAINT fk_fact_bin_visit_connectivity
        FOREIGN KEY (connectivity_state_key) REFERENCES dim_connectivity_state (connectivity_state_key),

    CONSTRAINT chk_fact_bin_visit_measure_bundle
        CHECK (visit_count >= emptied_visit_count AND visit_count >= not_emptied_visit_count)
);

CREATE TABLE fact_vehicle_emptying
(
    vehicle_emptying_key   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tour_id                BIGINT      NOT NULL,
    sequence_in_tour       INTEGER     NOT NULL CHECK (sequence_in_tour >= 1),
    event_ts               TIMESTAMPTZ NOT NULL,
    date_key               INTEGER     NOT NULL,
    vehicle_key            BIGINT      NOT NULL,
    connectivity_state_key SMALLINT    NOT NULL,
    vehicle_emptying_count SMALLINT    NOT NULL DEFAULT 1 CHECK (vehicle_emptying_count >= 0),

    CONSTRAINT fk_fact_vehicle_emptying_date
        FOREIGN KEY (date_key) REFERENCES dim_date (date_key),
    CONSTRAINT fk_fact_vehicle_emptying_vehicle
        FOREIGN KEY (vehicle_key) REFERENCES dim_vehicle (vehicle_key),
    CONSTRAINT fk_fact_vehicle_emptying_connectivity
        FOREIGN KEY (connectivity_state_key) REFERENCES dim_connectivity_state (connectivity_state_key)
);

CREATE TABLE fact_bin_daily_snapshot
(
    bin_daily_snapshot_key       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_key                     INTEGER NOT NULL,
    bin_key                      BIGINT  NOT NULL,
    zone_key                     BIGINT,
    -- as of the end of the day, could have been inactive during the day
    is_active                    BOOLEAN NOT NULL DEFAULT FALSE,
    visit_count                  INTEGER NOT NULL CHECK (visit_count >= 0),
    emptied_visit_count          INTEGER NOT NULL CHECK (emptied_visit_count >= 0),
    distinct_tour_count          INTEGER NOT NULL CHECK (distinct_tour_count >= 0),
    visited_flag                 BOOLEAN NOT NULL DEFAULT FALSE,
    emptied_flag                 BOOLEAN NOT NULL DEFAULT FALSE,
    low_fill_visit_count         INTEGER NOT NULL DEFAULT 0 CHECK (low_fill_visit_count >= 0),
    low_fill_emptied_count       INTEGER NOT NULL DEFAULT 0 CHECK (low_fill_emptied_count >= 0),
    high_fill_visit_count        INTEGER NOT NULL DEFAULT 0 CHECK (high_fill_visit_count >= 0),
    high_fill_emptied_count      INTEGER NOT NULL DEFAULT 0 CHECK (high_fill_emptied_count >= 0),
    overfull_visit_count         INTEGER NOT NULL DEFAULT 0 CHECK (overfull_visit_count >= 0),
    overfull_emptied_count       INTEGER NOT NULL DEFAULT 0 CHECK (overfull_emptied_count >= 0),
    last_observed_fill_level_key SMALLINT,
    max_observed_fill_level_key  SMALLINT,
    event_affected_flag          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_fact_bin_daily_snapshot_date
        FOREIGN KEY (date_key) REFERENCES dim_date (date_key),
    CONSTRAINT fk_fact_bin_daily_snapshot_bin
        FOREIGN KEY (bin_key) REFERENCES dim_bin (bin_key),
    CONSTRAINT fk_fact_bin_daily_snapshot_zone
        FOREIGN KEY (zone_key) REFERENCES dim_zone (zone_key),
    CONSTRAINT fk_fact_bin_daily_snapshot_last_fill
        FOREIGN KEY (last_observed_fill_level_key) REFERENCES dim_fill_level (fill_level_key),
    CONSTRAINT fk_fact_bin_daily_snapshot_max_fill
        FOREIGN KEY (max_observed_fill_level_key) REFERENCES dim_fill_level (fill_level_key),

    CONSTRAINT uq_fact_bin_daily_snapshot UNIQUE (date_key, bin_key)
);

CREATE TABLE fact_bin_status_change
(
    bin_status_change_key BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bin_activity_id       BIGINT      NOT NULL UNIQUE,
    bin_key               BIGINT      NOT NULL,
    date_key              INTEGER     NOT NULL,
    activity_ts           TIMESTAMPTZ NOT NULL,
    active_flag           BOOLEAN     NOT NULL,

    CONSTRAINT fk_fact_bin_status_change_bin
        FOREIGN KEY (bin_key) REFERENCES dim_bin (bin_key),
    CONSTRAINT fk_fact_bin_status_change_date
        FOREIGN KEY (date_key) REFERENCES dim_date (date_key)
);

CREATE TABLE fact_weather_day
(
    weather_day_key         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date_key                INTEGER NOT NULL UNIQUE,
    temp_avg_c              NUMERIC(5, 2),
    temp_max_c              NUMERIC(5, 2),
    sunshine_duration_hours NUMERIC(6, 2) CHECK (sunshine_duration_hours IS NULL OR sunshine_duration_hours >= 0),
    precipitation_mm        NUMERIC(8, 2) CHECK (precipitation_mm IS NULL OR precipitation_mm >= 0),

    CONSTRAINT fk_fact_weather_day_date
        FOREIGN KEY (date_key) REFERENCES dim_date (date_key)
);

CREATE TABLE fact_event_zone_day
(
    event_zone_day_key BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_key          BIGINT  NOT NULL,
    zone_key           BIGINT  NOT NULL,
    date_key           INTEGER NOT NULL,

    CONSTRAINT fk_fact_event_zone_day_event
        FOREIGN KEY (event_key) REFERENCES dim_event (event_key),
    CONSTRAINT fk_fact_event_zone_day_zone
        FOREIGN KEY (zone_key) REFERENCES dim_zone (zone_key),
    CONSTRAINT fk_fact_event_zone_day_date
        FOREIGN KEY (date_key) REFERENCES dim_date (date_key),

    CONSTRAINT uq_fact_event_zone_day UNIQUE (event_key, zone_key, date_key)
);

-- =========================================================
-- indexes
-- =========================================================

CREATE INDEX idx_fact_bin_visit_date_key
    ON fact_bin_visit (date_key);

CREATE INDEX idx_fact_bin_visit_bin_key
    ON fact_bin_visit (bin_key);

CREATE INDEX idx_fact_bin_visit_vehicle_key
    ON fact_bin_visit (vehicle_key);

CREATE INDEX idx_fact_bin_visit_tour_id
    ON fact_bin_visit (tour_id);

CREATE INDEX idx_fact_bin_visit_event_ts
    ON fact_bin_visit (event_ts);

CREATE INDEX idx_fact_tour_vehicle_key
    ON fact_tour (vehicle_key);

CREATE INDEX idx_fact_tour_date_key
    ON fact_tour (date_key);

CREATE INDEX idx_fact_vehicle_emptying_date_key
    ON fact_vehicle_emptying (date_key);

CREATE INDEX idx_fact_vehicle_emptying_vehicle_key
    ON fact_vehicle_emptying (vehicle_key);

CREATE INDEX idx_fact_vehicle_emptying_tour_id
    ON fact_vehicle_emptying (tour_id);

CREATE INDEX idx_fact_bin_daily_snapshot_date_key
    ON fact_bin_daily_snapshot (date_key);

CREATE INDEX idx_fact_bin_daily_snapshot_bin_key
    ON fact_bin_daily_snapshot (bin_key);

CREATE INDEX idx_fact_bin_daily_snapshot_zone_key
    ON fact_bin_daily_snapshot (zone_key);

CREATE INDEX idx_fact_bin_status_change_date_key
    ON fact_bin_status_change (date_key);

CREATE INDEX idx_fact_bin_status_change_bin_key
    ON fact_bin_status_change (bin_key);

