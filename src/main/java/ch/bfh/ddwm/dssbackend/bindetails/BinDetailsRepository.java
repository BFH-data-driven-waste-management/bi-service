package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.model.BinDetails;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinFeatureSnapshot;
import ch.bfh.ddwm.dssbackend.jooq.generated.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.FactBinDay;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.FactBinFeatureSnapshot;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class BinDetailsRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final FactBinDay FACT_BIN_DAY = Tables.FACT_BIN_DAY;
    private static final FactBinFeatureSnapshot FACT_BIN_FEATURE_SNAPSHOT = Tables.FACT_BIN_FEATURE_SNAPSHOT;

    private final DSLContext dsl;

    public BinDetailsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Integer findLatestFactBinDayDateKey() {
        return dsl
                .select(DSL.max(FACT_BIN_DAY.DATE_KEY))
                .from(FACT_BIN_DAY)
                .fetchOne(0, Integer.class);
    }

    public Integer findLatestFeatureSnapshotDateKey(int maxDateKeyInclusive) {
        return dsl
                .select(DSL.max(FACT_BIN_FEATURE_SNAPSHOT.DATE_KEY))
                .from(FACT_BIN_FEATURE_SNAPSHOT)
                .where(FACT_BIN_FEATURE_SNAPSHOT.DATE_KEY.le(maxDateKeyInclusive))
                .fetchOne(0, Integer.class);
    }

    public BinDetails findBinDetailsByBinKeyAndDate(long binKey, int featureDateKey) {
        return dsl
                .select(
                        FACT_BIN_FEATURE_SNAPSHOT.BIN_KEY,
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.VOLUME_LITERS,
                        DIM_BIN.ZONE_KEY,
                        DIM_BIN.CURRENT_ACTIVE_FLAG,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326,
                        FACT_BIN_FEATURE_SNAPSHOT.BASELINE_AVG_VISITS_PER_WEEK_90D,
                        FACT_BIN_FEATURE_SNAPSHOT.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D,
                        FACT_BIN_FEATURE_SNAPSHOT.LOW_FILL_VISIT_RATIO_90D,
                        FACT_BIN_FEATURE_SNAPSHOT.NOT_EMPTIED_RATIO_90D,
                        FACT_BIN_FEATURE_SNAPSHOT.EMPTYING_RANK_90D,
                        FACT_BIN_FEATURE_SNAPSHOT.WEATHER_SENSITIVITY_SCORE,
                        FACT_BIN_FEATURE_SNAPSHOT.RAIN_SENSITIVITY_SCORE,
                        FACT_BIN_FEATURE_SNAPSHOT.SUN_SENSITIVITY_SCORE,
                        FACT_BIN_FEATURE_SNAPSHOT.HEAT_SENSITIVITY_SCORE,
                        FACT_BIN_FEATURE_SNAPSHOT.EVENT_SENSITIVITY_SCORE
                )
                .from(FACT_BIN_FEATURE_SNAPSHOT)
                .join(DIM_BIN).on(DIM_BIN.BIN_ID.eq(FACT_BIN_FEATURE_SNAPSHOT.BIN_KEY))
                .where(FACT_BIN_FEATURE_SNAPSHOT.DATE_KEY.eq(featureDateKey))
                .and(FACT_BIN_FEATURE_SNAPSHOT.BIN_KEY.eq(binKey))
                .fetchOne(record -> new BinDetails(
                        record.get(FACT_BIN_FEATURE_SNAPSHOT.BIN_KEY),
                        record.get(DIM_BIN.BIN_TYPE),
                        record.get(DIM_BIN.VOLUME_LITERS),
                        record.get(DIM_BIN.ZONE_KEY),
                        Boolean.TRUE.equals(record.get(DIM_BIN.CURRENT_ACTIVE_FLAG)),
                        record.get(DIM_BIN.COORD_X_2056),
                        record.get(DIM_BIN.COORD_Y_2056),
                        record.get(DIM_BIN.COORD_X_4326),
                        record.get(DIM_BIN.COORD_Y_4326),
                        new BinFeatureSnapshot(
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.BASELINE_AVG_VISITS_PER_WEEK_90D),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.BASELINE_AVG_EMPTYINGS_PER_WEEK_90D),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.LOW_FILL_VISIT_RATIO_90D),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.NOT_EMPTIED_RATIO_90D),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.EMPTYING_RANK_90D),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.WEATHER_SENSITIVITY_SCORE),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.RAIN_SENSITIVITY_SCORE),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.SUN_SENSITIVITY_SCORE),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.HEAT_SENSITIVITY_SCORE),
                                record.get(FACT_BIN_FEATURE_SNAPSHOT.EVENT_SENSITIVITY_SCORE)
                        )
                ));
    }
}
