package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.bindetails.model.BinDetails;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinDayFeatures;
import ch.bfh.ddwm.dssbackend.binlist.model.BinListItem;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimBin;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BinListRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.tables.BinDayFeatures BIN_DAY_FEATURES = ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.Tables.BIN_DAY_FEATURES;
    private final DSLContext dsl;

    public BinListRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Integer findLatestBinDayFeaturesDateKey() {
        return dsl
                .select(DSL.max(BIN_DAY_FEATURES.DATE_KEY))
                .from(BIN_DAY_FEATURES)
                .fetchOne(0, Integer.class);
    }

    public List<BinListItem> findBinListByDateRange(int startDateKeyInclusive, int endDateKeyInclusive) {
        return dsl
                .select(
                        BIN_DAY_FEATURES.BIN_KEY,
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.VOLUME_LITERS,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326,
                        BIN_DAY_FEATURES.BASELINE_AVG_VISITS_PER_WEEK_90D,
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
                .join(DIM_BIN).on(DIM_BIN.BIN_ID.eq(BIN_DAY_FEATURES.BIN_KEY))
                .where(BIN_DAY_FEATURES.DATE_KEY.eq(featureDateKey))
                .and(BIN_DAY_FEATURES.BIN_KEY.eq(binKey))
                .fetchOne(record -> new BinDetails(
                        record.get(DIM_BIN.BIN_ID),
                        record.get(DIM_BIN.BIN_TYPE),
                        record.get(DIM_BIN.VOLUME_LITERS),
                        record.get(DIM_BIN.COORD_X_2056),
                        record.get(DIM_BIN.COORD_Y_2056),
                        record.get(DIM_BIN.COORD_X_4326),
                        record.get(DIM_BIN.COORD_Y_4326),
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
                ));
    }
}
