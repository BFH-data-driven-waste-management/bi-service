package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.binlist.model.BinListItem;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactBinDailySnapshot;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BinListRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final FactBinDailySnapshot FACT_BIN_DAILY_SNAPSHOT = Tables.FACT_BIN_DAILY_SNAPSHOT;
    private static final ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.tables.BinDayFeatures BIN_DAY_FEATURES = ch.bfh.ddwm.dssbackend.jooq.generated.analytics_derived.Tables.BIN_DAY_FEATURES;
    private final DSLContext dsl;

    public BinListRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PageResponse<BinListItem> findBinListByDateKey(int dateKey, int page, int size) {
        List<BinListItem> content = dsl
                .select(
                        DIM_BIN.BIN_ID,
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056,
                        FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE,
                        BIN_DAY_FEATURES.BASELINE_AVG_VISITS_PER_WEEK_90D,
                        BIN_DAY_FEATURES.LOW_FILL_VISIT_RATIO_90D,
                        BIN_DAY_FEATURES.OVERFULL_VISIT_RATIO_90D
                )
                .from(FACT_BIN_DAILY_SNAPSHOT)
                .join(DIM_BIN).on(DIM_BIN.BIN_KEY.eq(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY))
                .join(BIN_DAY_FEATURES)
                .on(BIN_DAY_FEATURES.BIN_KEY.eq(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY)
                        .and(BIN_DAY_FEATURES.DATE_KEY.eq(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY)))
                .where(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY.eq(dateKey))
                .orderBy(DIM_BIN.BIN_ID.asc())
                .limit(size)
                .offset(page * size)
                .fetch(record -> new BinListItem(
                        record.get(DIM_BIN.BIN_ID),
                        record.get(DIM_BIN.BIN_TYPE),
                        Boolean.TRUE.equals(record.get(FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE)),
                        record.get(BIN_DAY_FEATURES.BASELINE_AVG_VISITS_PER_WEEK_90D),
                        record.get(BIN_DAY_FEATURES.LOW_FILL_VISIT_RATIO_90D),
                        record.get(BIN_DAY_FEATURES.OVERFULL_VISIT_RATIO_90D),
                        record.get(DIM_BIN.COORD_X_2056),
                        record.get(DIM_BIN.COORD_Y_2056)
                ));

        int totalElements = countTotalElements(dateKey);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    private int countTotalElements(int dateKey) {
        var totalElementsValue = dsl.selectCount()
                .from(FACT_BIN_DAILY_SNAPSHOT)
                .where(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY.eq(dateKey))
                .fetchOne(0, Integer.class);

        return totalElementsValue == null ? 0 : totalElementsValue;
    }
}
