package ch.bfh.ddwm.dssbackend.binmap;

import ch.bfh.ddwm.dssbackend.binmap.model.BinMapItem;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactBinDailySnapshot;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class BinMapRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final FactBinDailySnapshot FACT_BIN_DAILY_SNAPSHOT = Tables.FACT_BIN_DAILY_SNAPSHOT;
    private static final ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.tables.BinDayFeatures BIN_DAY_FEATURES =
            ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.Tables.BIN_DAY_FEATURES;

    private final DSLContext dsl;

    public BinMapRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<BinMapItem> findBinMapByDateKey(int dateKey) {
        return dsl
                .select(
                        DIM_BIN.BIN_ID,
                        DIM_BIN.BIN_TYPE,
                        FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056,
                        BIN_DAY_FEATURES.DAYS_SINCE_LAST_EMPTYING
                )
                .from(FACT_BIN_DAILY_SNAPSHOT)
                .join(DIM_BIN).on(DIM_BIN.BIN_KEY.eq(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY))
                .join(BIN_DAY_FEATURES).on(BIN_DAY_FEATURES.BIN_KEY.eq(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY)
                        .and(BIN_DAY_FEATURES.DATE_KEY.eq(dateKey)))
                .where(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY.eq(dateKey))
                .orderBy(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY.asc())
                .fetch(record -> new BinMapItem(
                        record.get(DIM_BIN.BIN_ID),
                        record.get(DIM_BIN.BIN_TYPE),
                        record.get(FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE),
                        record.get(DIM_BIN.COORD_X_4326),
                        record.get(DIM_BIN.COORD_Y_4326),
                        record.get(DIM_BIN.COORD_X_2056),
                        record.get(DIM_BIN.COORD_Y_2056),
                        record.get(BIN_DAY_FEATURES.DAYS_SINCE_LAST_EMPTYING)
                ));
    }

    public Map<Long, BigDecimal> findAvgDailyAdditionOverTimeWindowPerBin(int dateKey, int windowStartDateKey) {
        var averageFillByBin = dsl
                .select(
                        BIN_DAY_FEATURES.BIN_KEY.as("bin_key"),
                        DSL.avg(BIN_DAY_FEATURES.ESTIMATED_FILL_ADDITION_PER_DAY).as("avg_estimated_fill_addition")
                )
                .from(BIN_DAY_FEATURES)
                .where(BIN_DAY_FEATURES.DATE_KEY.between(windowStartDateKey, dateKey))
                .and(BIN_DAY_FEATURES.ESTIMATED_FILL_ADDITION_PER_DAY.isNotNull())
                .groupBy(BIN_DAY_FEATURES.BIN_KEY)
                .asTable("average_fill_by_bin");

        Field<BigDecimal> avgEstimatedFillAddition = averageFillByBin.field("avg_estimated_fill_addition", BigDecimal.class);

        return dsl
                .select(
                        DIM_BIN.BIN_ID,
                        avgEstimatedFillAddition
                )
                .from(averageFillByBin)
                .join(DIM_BIN).on(DIM_BIN.BIN_KEY.eq(averageFillByBin.field("bin_key", Long.class)))
                .stream()
                .collect(Collectors.toMap(
                        record -> record.get(DIM_BIN.BIN_ID),
                        record -> record.get(avgEstimatedFillAddition)
                ));
    }
}
