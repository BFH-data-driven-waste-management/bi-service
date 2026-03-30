package ch.bfh.ddwm.dssbackend.dashboard;

import ch.bfh.ddwm.dssbackend.dashboard.model.CountOfBinType;
import ch.bfh.ddwm.dssbackend.dashboard.model.SystemDayAggregated;
import ch.bfh.ddwm.dssbackend.jooq.generated.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.FactSystemDay;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class DashboardRepository {

    private static final FactSystemDay FACT_SYSTEM_DAY = Tables.FACT_SYSTEM_DAY;
    private static final DimBin DIM_BIN = Tables.DIM_BIN;

    private final DSLContext dsl;

    public DashboardRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<SystemDayAggregated> latestSystemDayAggregatedByPredecessor(
            int currentDateKey,
            int previous7DateKey,
            int previous30DateKey,
            int previous90DateKey
    ) {
        var curr = FACT_SYSTEM_DAY.as("curr");
        var prev7 = FACT_SYSTEM_DAY.as("prev7");
        var prev30 = FACT_SYSTEM_DAY.as("prev30");
        var prev90 = FACT_SYSTEM_DAY.as("prev90");

        var activeBinCount = curr.ACTIVE_BIN_COUNT.as("active_bin_count");

        var visits7dCurrent = curr.BIN_VISIT_COUNT_7D.as("visits_7d_current");
        var visits7dPrevious = DSL.coalesce(prev7.BIN_VISIT_COUNT_7D, 0).as("visits_7d_previous");

        var emptyings7dCurrent = curr.EMPTIED_VISIT_COUNT_7D.as("emptyings_7d_current");
        var emptyings7dPrevious = DSL.coalesce(prev7.EMPTIED_VISIT_COUNT_7D, 0).as("emptyings_7d_previous");

        var emptyingRate7dCurrent = curr.VISIT_EMPTIED_RATIO_7D.as("emptying_rate_7d_current");
        var emptyingRate7dPrevious = DSL.coalesce(prev7.VISIT_EMPTIED_RATIO_7D, BigDecimal.ZERO).as("emptying_rate_7d_previous");

        var lowFillVisitShare90dCurrent = curr.LOW_FILL_VISIT_RATIO_90D.as("low_fill_visit_share_90d_current");
        var lowFillVisitShare90dPrevious = DSL.coalesce(prev90.LOW_FILL_VISIT_RATIO_90D, BigDecimal.ZERO).as("low_fill_visit_share_90d_previous");

        var lowFillEmptyingShare90dCurrent = curr.LOW_FILL_EMPTIED_RATIO_90D.as("low_fill_emptying_share_90d_current");
        var lowFillEmptyingShare90dPrevious = DSL.coalesce(prev90.LOW_FILL_EMPTIED_RATIO_90D, BigDecimal.ZERO).as("low_fill_emptying_share_90d_previous");

        var overfullEvents30dCurrent = curr.OVERFULL_VISIT_30D.as("overfull_events_30d_current");
        var overfullEvents30dPrevious = DSL.coalesce(prev30.OVERFULL_VISIT_30D, 0).as("overfull_events_30d_previous");

        var result = dsl
                .select(
                        activeBinCount,
                        visits7dCurrent,
                        visits7dPrevious,
                        emptyings7dCurrent,
                        emptyings7dPrevious,
                        emptyingRate7dCurrent,
                        emptyingRate7dPrevious,
                        lowFillVisitShare90dCurrent,
                        lowFillVisitShare90dPrevious,
                        lowFillEmptyingShare90dCurrent,
                        lowFillEmptyingShare90dPrevious,
                        overfullEvents30dCurrent,
                        overfullEvents30dPrevious
                )
                .from(curr)
                .leftJoin(prev7).on(prev7.DATE_KEY.eq(previous7DateKey))
                .leftJoin(prev30).on(prev30.DATE_KEY.eq(previous30DateKey))
                .leftJoin(prev90).on(prev90.DATE_KEY.eq(previous90DateKey))
                .where(curr.DATE_KEY.eq(currentDateKey))
                .fetchOne();

        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(new SystemDayAggregated(
                result.get(activeBinCount),
                result.get(visits7dCurrent),
                result.get(visits7dPrevious),
                result.get(emptyings7dCurrent),
                result.get(emptyings7dPrevious),
                result.get(emptyingRate7dCurrent),
                result.get(emptyingRate7dPrevious),
                result.get(lowFillVisitShare90dCurrent),
                result.get(lowFillVisitShare90dPrevious),
                result.get(lowFillEmptyingShare90dCurrent),
                result.get(lowFillEmptyingShare90dPrevious),
                result.get(overfullEvents30dCurrent),
                result.get(overfullEvents30dPrevious)
        ));
    }

    public List<CountOfBinType> findActiveBinCountByType() {
        var countField = DSL.count().as("cnt");
        return dsl
                .select(DIM_BIN.BIN_TYPE, countField)
                .from(DIM_BIN)
                .where(DIM_BIN.CURRENT_ACTIVE_FLAG.isTrue())
                .groupBy(DIM_BIN.BIN_TYPE)
                .orderBy(DIM_BIN.BIN_TYPE.asc())
                .fetch(record -> new CountOfBinType(
                        record.get(DIM_BIN.BIN_TYPE),
                        record.get(countField) != null ? record.get(countField) : 0L
                ));
    }

    public Integer findLatestAvailableDateKey(int maxDateKeyInclusive) {
        return dsl
                .select(DSL.max(FACT_SYSTEM_DAY.DATE_KEY))
                .from(FACT_SYSTEM_DAY)
                .where(FACT_SYSTEM_DAY.DATE_KEY.le(maxDateKeyInclusive))
                .fetchOne(0, Integer.class);
    }
}
