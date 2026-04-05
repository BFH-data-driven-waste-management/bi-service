package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.model.BinDetails;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinDayFeatures;
import ch.bfh.ddwm.dssbackend.bindetails.model.DailyCountPoint;
import ch.bfh.ddwm.dssbackend.common.DateKeyHelper;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimDate;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimFillLevel;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactBinDailySnapshot;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactBinVisit;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class BinDetailsRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final DimDate DIM_DATE = Tables.DIM_DATE;
    private static final DimFillLevel DIM_FILL_LEVEL = Tables.DIM_FILL_LEVEL;
    private static final FactBinDailySnapshot FACT_BIN_DAILY_SNAPSHOT = Tables.FACT_BIN_DAILY_SNAPSHOT;
    private static final FactBinVisit FACT_BIN_VISIT = Tables.FACT_BIN_VISIT;
    private static final ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.tables.BinDayFeatures BIN_DAY_FEATURES =
            ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.Tables.BIN_DAY_FEATURES;

    private final DSLContext dsl;

    public BinDetailsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public BinDetails binDayFeaturesByBinIdAndDateKey(long binId, int dateKey) {
        return dsl
                .select(
                        DIM_BIN.BIN_ID,
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.VOLUME_LITERS,
                        FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326,
                        BIN_DAY_FEATURES.DAYS_SINCE_LAST_VISIT,
                        BIN_DAY_FEATURES.DAYS_SINCE_LAST_EMPTYING,
                        BIN_DAY_FEATURES.BASELINE_AVG_VISITS_PER_WEEK_90D,
                        BIN_DAY_FEATURES.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D,
                        BIN_DAY_FEATURES.LOW_FILL_VISIT_RATIO_90D,
                        BIN_DAY_FEATURES.NOT_EMPTIED_RATIO_90D,
                        BIN_DAY_FEATURES.EMPTYING_RANK_90D,
                        BIN_DAY_FEATURES.WEATHER_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.RAIN_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.SUN_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.HEAT_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.EVENT_SENSITIVITY_SCORE
                )
                .from(BIN_DAY_FEATURES)
                .join(DIM_BIN).on(DIM_BIN.BIN_KEY.eq(BIN_DAY_FEATURES.BIN_KEY))
                .join(FACT_BIN_DAILY_SNAPSHOT)
                .on(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY.eq(BIN_DAY_FEATURES.BIN_KEY)
                        .and(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY.eq(BIN_DAY_FEATURES.DATE_KEY)))
                .where(BIN_DAY_FEATURES.DATE_KEY.eq(dateKey))
                .and(DIM_BIN.BIN_ID.eq(binId))
                .fetchOne(record -> {
                    Integer daysSinceLastVisit = record.get(BIN_DAY_FEATURES.DAYS_SINCE_LAST_VISIT);
                    Integer daysSinceLastEmptying = record.get(BIN_DAY_FEATURES.DAYS_SINCE_LAST_EMPTYING);

                    return new BinDetails(
                            record.get(DIM_BIN.BIN_ID),
                            record.get(DIM_BIN.BIN_TYPE),
                            record.get(DIM_BIN.VOLUME_LITERS),
                            Boolean.TRUE.equals(record.get(FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE)),
                            record.get(DIM_BIN.COORD_X_2056),
                            record.get(DIM_BIN.COORD_Y_2056),
                            record.get(DIM_BIN.COORD_X_4326),
                            record.get(DIM_BIN.COORD_Y_4326),
                            daysSinceLastVisit == null ? null : dateKeyMinusDays(dateKey, daysSinceLastVisit),
                            daysSinceLastEmptying == null ? null : dateKeyMinusDays(dateKey, daysSinceLastEmptying),
                            new BinDayFeatures(
                                    record.get(BIN_DAY_FEATURES.BASELINE_AVG_VISITS_PER_WEEK_90D),
                                    record.get(BIN_DAY_FEATURES.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D),
                                    record.get(BIN_DAY_FEATURES.LOW_FILL_VISIT_RATIO_90D),
                                    record.get(BIN_DAY_FEATURES.NOT_EMPTIED_RATIO_90D),
                                    record.get(BIN_DAY_FEATURES.EMPTYING_RANK_90D),
                                    record.get(BIN_DAY_FEATURES.WEATHER_SENSITIVITY_SCORE),
                                    record.get(BIN_DAY_FEATURES.RAIN_SENSITIVITY_SCORE),
                                    record.get(BIN_DAY_FEATURES.SUN_SENSITIVITY_SCORE),
                                    record.get(BIN_DAY_FEATURES.HEAT_SENSITIVITY_SCORE),
                                    record.get(BIN_DAY_FEATURES.EVENT_SENSITIVITY_SCORE)
                            )
                    );
                });
    }

    public Optional<BinDayFeatures> findFeatureSnapshotByBinIdAndDateKey(long binId, int dateKey) {
        return dsl
                .select(
                        BIN_DAY_FEATURES.BASELINE_AVG_VISITS_PER_WEEK_90D,
                        BIN_DAY_FEATURES.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D,
                        BIN_DAY_FEATURES.LOW_FILL_VISIT_RATIO_90D,
                        BIN_DAY_FEATURES.NOT_EMPTIED_RATIO_90D,
                        BIN_DAY_FEATURES.EMPTYING_RANK_90D,
                        BIN_DAY_FEATURES.WEATHER_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.RAIN_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.SUN_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.HEAT_SENSITIVITY_SCORE,
                        BIN_DAY_FEATURES.EVENT_SENSITIVITY_SCORE
                )
                .from(BIN_DAY_FEATURES)
                .join(DIM_BIN).on(DIM_BIN.BIN_KEY.eq(BIN_DAY_FEATURES.BIN_KEY))
                .where(BIN_DAY_FEATURES.DATE_KEY.eq(dateKey))
                .and(DIM_BIN.BIN_ID.eq(binId))
                .fetchOptional(record -> new BinDayFeatures(
                        record.get(BIN_DAY_FEATURES.BASELINE_AVG_VISITS_PER_WEEK_90D),
                        record.get(BIN_DAY_FEATURES.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D),
                        record.get(BIN_DAY_FEATURES.LOW_FILL_VISIT_RATIO_90D),
                        record.get(BIN_DAY_FEATURES.NOT_EMPTIED_RATIO_90D),
                        record.get(BIN_DAY_FEATURES.EMPTYING_RANK_90D),
                        record.get(BIN_DAY_FEATURES.WEATHER_SENSITIVITY_SCORE),
                        record.get(BIN_DAY_FEATURES.RAIN_SENSITIVITY_SCORE),
                        record.get(BIN_DAY_FEATURES.SUN_SENSITIVITY_SCORE),
                        record.get(BIN_DAY_FEATURES.HEAT_SENSITIVITY_SCORE),
                        record.get(BIN_DAY_FEATURES.EVENT_SENSITIVITY_SCORE)
                ));
    }

    public List<DailyCountPoint> findVisitFrequencyPerWeekInWindow(long binId, int startDateKeyInclusive, int endDateKeyInclusive) {
        return findFrequencyPerWeekInWindowOfField(binId, startDateKeyInclusive, endDateKeyInclusive, FACT_BIN_DAILY_SNAPSHOT.VISIT_COUNT);
    }

    public List<DailyCountPoint> findEmptyingFrequencyPerWeekInWindow(long binId, int startDateKeyInclusive, int endDateKeyInclusive) {
        return findFrequencyPerWeekInWindowOfField(binId, startDateKeyInclusive, endDateKeyInclusive, FACT_BIN_DAILY_SNAPSHOT.EMPTIED_VISIT_COUNT);
    }


    /*
     * centralized logic for weekly averages over given window
     * idea: group by custom Field<> which projects every date_key to its week's start date key
     */
    private List<DailyCountPoint> findFrequencyPerWeekInWindowOfField(long binId, int startDateKeyInclusive, int endDateKeyInclusive, Field<Integer> countField) {
        Field<Integer> weekStartDateKey = weekStartDateKeyField(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY);
        Field<BigDecimal> averageCountPerWeek = DSL.avg(countField.cast(BigDecimal.class))
                .as("average_count_per_week");

        return dsl
                .select(weekStartDateKey, averageCountPerWeek)
                .from(FACT_BIN_DAILY_SNAPSHOT)
                .join(DIM_BIN).on(DIM_BIN.BIN_KEY.eq(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY))
                .where(DIM_BIN.BIN_ID.eq(binId))
                .and(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY.between(startDateKeyInclusive, endDateKeyInclusive))
                .groupBy(weekStartDateKey)
                .orderBy(weekStartDateKey.asc())
                .fetch(record -> new DailyCountPoint(
                        record.get(weekStartDateKey),
                        record.get(averageCountPerWeek)
                ));
    }

    public List<DailyCountPoint> findFillTrend12m(long binId, int startDateKeyInclusive, int endDateKeyInclusive) {
        Field<Integer> weekStartDateKey = weekStartDateKeyField(FACT_BIN_VISIT.DATE_KEY);

        Field<BigDecimal> fillScore = DSL
                .when(DIM_FILL_LEVEL.FILL_LEVEL_CODE.eq("OVERFULL"), BigDecimal.valueOf(0.875))
                .when(DIM_FILL_LEVEL.FILL_LEVEL_CODE.eq("FULL"), BigDecimal.valueOf(0.625))
                .when(DIM_FILL_LEVEL.FILL_LEVEL_CODE.eq("HALF_FULL"), BigDecimal.valueOf(0.375))
                .when(DIM_FILL_LEVEL.FILL_LEVEL_CODE.eq("EMPTY_OR_ALMOST_EMPTY"), BigDecimal.valueOf(0.125))
                .otherwise(BigDecimal.ZERO);

        Field<BigDecimal> avgFillScore = DSL.avg(fillScore).as("avg_fill_score");

        return dsl
                .select(weekStartDateKey, avgFillScore)
                .from(FACT_BIN_VISIT)
                .join(DIM_BIN).on(DIM_BIN.BIN_KEY.eq(FACT_BIN_VISIT.BIN_KEY))
                .join(DIM_DATE).on(DIM_DATE.DATE_KEY.eq(FACT_BIN_VISIT.DATE_KEY))
                .join(DIM_FILL_LEVEL).on(DIM_FILL_LEVEL.FILL_LEVEL_KEY.eq(FACT_BIN_VISIT.FILL_LEVEL_KEY))
                .where(DIM_BIN.BIN_ID.eq(binId))
                .and(FACT_BIN_VISIT.DATE_KEY.between(startDateKeyInclusive, endDateKeyInclusive))
                .groupBy(weekStartDateKey)
                .orderBy(weekStartDateKey.asc())
                .fetch(record -> new DailyCountPoint(
                        record.get(weekStartDateKey),
                        record.get(avgFillScore)
                ));
    }

    private int dateKeyMinusDays(int dateKey, int days) {
        java.time.LocalDate date = DateKeyHelper.fromDateKey(dateKey);
        java.time.LocalDate adjusted = date.minusDays(days);
        return DateKeyHelper.toDateKey(adjusted);
    }

    private Field<Integer> weekStartDateKeyField(Field<Integer> dateKeyField) {
        return DSL.field(
                "CAST(to_char(date_trunc('week', to_date(CAST({0} AS text), 'YYYYMMDD')), 'YYYYMMDD') AS integer)",
                Integer.class,
                dateKeyField
        );
    }
}
