package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.binlist.model.BinListItem;
import ch.bfh.ddwm.dssbackend.jooq.generated.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.FactBinDay;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class BinListRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final FactBinDay FACT_BIN_DAY = Tables.FACT_BIN_DAY;

    private final DSLContext dsl;

    public BinListRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Integer findLatestFactBinDayDateKey() {
        return dsl
                .select(DSL.max(FACT_BIN_DAY.DATE_KEY))
                .from(FACT_BIN_DAY)
                .fetchOne(0, Integer.class);
    }

    public List<BinListItem> findBinListByDateRange(int startDateKeyInclusive, int endDateKeyInclusive) {
        var sumVisitCount = DSL.sum(FACT_BIN_DAY.VISIT_COUNT);
        var sumLowFillVisitCount = DSL.sum(FACT_BIN_DAY.LOW_FILL_VISIT_COUNT);
        var sumOverfullVisitCount = DSL.sum(FACT_BIN_DAY.OVERFULL_VISIT_COUNT);

        var visitCountDecimal = DSL.coalesce(sumVisitCount.cast(BigDecimal.class), BigDecimal.ZERO);
        var lowFillVisitCountDecimal = DSL.coalesce(sumLowFillVisitCount.cast(BigDecimal.class), BigDecimal.ZERO);
        var overfullVisitCountDecimal = DSL.coalesce(sumOverfullVisitCount.cast(BigDecimal.class), BigDecimal.ZERO);

        var avgWeeklyVisits90d = visitCountDecimal
                .mul(BigDecimal.valueOf(7))
                .div(BigDecimal.valueOf(90))
                .as("avg_weekly_visits_90d");

        var lowFillVisitRatio90d = DSL.coalesce(
                        lowFillVisitCountDecimal.div(DSL.nullif(visitCountDecimal, BigDecimal.ZERO)),
                        BigDecimal.ZERO
                )
                .as("low_fill_visit_ratio_90d");

        var overfullVisitRatio90d = DSL.coalesce(
                        overfullVisitCountDecimal.div(DSL.nullif(visitCountDecimal, BigDecimal.ZERO)),
                        BigDecimal.ZERO
                )
                .as("overfull_visit_ratio_90d");

        return dsl
                .select(
                        FACT_BIN_DAY.BIN_KEY,
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.CURRENT_ACTIVE_FLAG,
                        avgWeeklyVisits90d,
                        lowFillVisitRatio90d,
                        overfullVisitRatio90d
                )
                .from(FACT_BIN_DAY)
                .join(DIM_BIN).on(DIM_BIN.BIN_ID.eq(FACT_BIN_DAY.BIN_KEY))
                .where(FACT_BIN_DAY.DATE_KEY.between(startDateKeyInclusive, endDateKeyInclusive))
                .groupBy(FACT_BIN_DAY.BIN_KEY, DIM_BIN.BIN_TYPE, DIM_BIN.CURRENT_ACTIVE_FLAG)
                .orderBy(FACT_BIN_DAY.BIN_KEY.asc())
                .fetch(record -> new BinListItem(
                        record.get(FACT_BIN_DAY.BIN_KEY),
                        record.get(DIM_BIN.BIN_TYPE),
                        Boolean.TRUE.equals(record.get(DIM_BIN.CURRENT_ACTIVE_FLAG)),
                        record.get(avgWeeklyVisits90d),
                        record.get(lowFillVisitRatio90d),
                        record.get(overfullVisitRatio90d)
                ));
    }
}
