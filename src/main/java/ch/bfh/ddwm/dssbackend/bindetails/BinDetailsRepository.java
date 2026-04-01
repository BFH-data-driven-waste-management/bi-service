package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.model.BinDetails;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinFeatureSnapshot;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactBinDailySnapshot;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.tables.BinDayFeatures;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class BinDetailsRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final FactBinDailySnapshot FACT_BIN_DAILY_SNAPSHOT = Tables.FACT_BIN_DAILY_SNAPSHOT;
    private static final BinDayFeatures BIN_DAY_Features = ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.Tables.BIN_DAY_FEATURES;

    private final DSLContext dsl;

    public BinDetailsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Integer findLatestFactBinDayDateKey() {
        return dsl
                .select(DSL.max(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY))
                .from(FACT_BIN_DAILY_SNAPSHOT)
                .fetchOne(0, Integer.class);
    }

    public Integer findLatestFeatureSnapshotDateKey(int maxDateKeyInclusive) {
        return dsl
                .select(DSL.max(BIN_DAY_Features.DATE_KEY))
                .from(BIN_DAY_Features)
                .where(BIN_DAY_Features.DATE_KEY.le(maxDateKeyInclusive))
                .fetchOne(0, Integer.class);
    }

    public BinDetails findBinDetailsByBinKeyAndDate(long binKey, int featureDateKey) {
        return dsl
                .select(
                        BIN_DAY_Features.BIN_KEY,
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.VOLUME_LITERS,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326,
                        BIN_DAY_Features.BASELINE_AVG_VISITS_PER_WEEK_90D,
                        BIN_DAY_Features.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D,
                        BIN_DAY_Features.LOW_FILL_VISIT_RATIO_90D,
                        BIN_DAY_Features.NOT_EMPTIED_RATIO_90D,
                        BIN_DAY_Features.EMPTYING_RANK_90D,
                        BIN_DAY_Features.WEATHER_SENSITIVITY_SCORE,
                        BIN_DAY_Features.RAIN_SENSITIVITY_SCORE,
                        BIN_DAY_Features.SUN_SENSITIVITY_SCORE,
                        BIN_DAY_Features.HEAT_SENSITIVITY_SCORE,
                        BIN_DAY_Features.EVENT_SENSITIVITY_SCORE
                )
                .from(BIN_DAY_Features)
                .join(DIM_BIN).on(DIM_BIN.BIN_ID.eq(BIN_DAY_Features.BIN_KEY))
                .where(BIN_DAY_Features.DATE_KEY.eq(featureDateKey))
                .and(BIN_DAY_Features.BIN_KEY.eq(binKey))
                .fetchOne(record -> new BinDetails(
                        record.get(BIN_DAY_Features.BIN_KEY),
                        record.get(DIM_BIN.BIN_TYPE),
                        record.get(DIM_BIN.VOLUME_LITERS),
                        record.get(DIM_BIN.COORD_X_2056),
                        record.get(DIM_BIN.COORD_Y_2056),
                        record.get(DIM_BIN.COORD_X_4326),
                        record.get(DIM_BIN.COORD_Y_4326),
                        new BinFeatureSnapshot(
                                record.get(BIN_DAY_Features.BASELINE_AVG_VISITS_PER_WEEK_90D),
                                record.get(BIN_DAY_Features.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D),
                                record.get(BIN_DAY_Features.LOW_FILL_VISIT_RATIO_90D),
                                record.get(BIN_DAY_Features.NOT_EMPTIED_RATIO_90D),
                                record.get(BIN_DAY_Features.EMPTYING_RANK_90D),
                                record.get(BIN_DAY_Features.WEATHER_SENSITIVITY_SCORE),
                                record.get(BIN_DAY_Features.RAIN_SENSITIVITY_SCORE),
                                record.get(BIN_DAY_Features.SUN_SENSITIVITY_SCORE),
                                record.get(BIN_DAY_Features.HEAT_SENSITIVITY_SCORE),
                                record.get(BIN_DAY_Features.EVENT_SENSITIVITY_SCORE)
                        )
                ));
    }
}
