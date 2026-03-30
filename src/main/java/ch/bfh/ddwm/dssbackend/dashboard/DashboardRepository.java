package ch.bfh.ddwm.dssbackend.dashboard;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import static ch.bfh.ddwm.dssbackend.jooq.generated.tables.FactSystemDay.FACT_SYSTEM_DAY;
import static org.jooq.impl.DSL.field;


@Repository
public class DashboardRepository {

    private final DSLContext dsl;

    public DashboardRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public DashboardRawData fetchDashboardRawData(
            int currentDateKey,
            int previous7DateKey,
            int previous30DateKey,
            int previous90DateKey
    ) {
        var curr = FACT_SYSTEM_DAY.as("curr");
        var prev7 = FACT_SYSTEM_DAY.as("prev7");
        var prev30 = FACT_SYSTEM_DAY.as("prev30");
        var prev90 = FACT_SYSTEM_DAY.as("prev90");

        var byTypeJsonField = field("""
                (
                  SELECT COALESCE(
                    json_agg(
                      json_build_object(
                        'label', grouped.bin_type,
                        'count', grouped.cnt
                      )
                      ORDER BY grouped.bin_type
                    )::text,
                    '[]'
                  )
                  FROM (
                    SELECT dim_bin.bin_type, COUNT(*) AS cnt
                    FROM analytics.dim_bin dim_bin
                    WHERE dim_bin.current_active_flag = true
                    GROUP BY dim_bin.bin_type
                  ) grouped
                )
                """, String.class).as("by_type_json");

        var result = dsl
                .select(
                        curr.ACTIVE_BIN_COUNT,

                        curr.BIN_VISIT_COUNT_7D,
                        prev7.BIN_VISIT_COUNT_7D,

                        curr.EMPTIED_VISIT_COUNT_7D,
                        prev7.EMPTIED_VISIT_COUNT_7D,

                        curr.VISIT_EMPTIED_RATIO_7D,
                        prev7.VISIT_EMPTIED_RATIO_7D,

                        curr.LOW_FILL_VISIT_RATIO_90D,
                        prev90.LOW_FILL_VISIT_RATIO_90D,

                        curr.LOW_FILL_EMPTIED_RATIO_90D,
                        prev90.LOW_FILL_EMPTIED_RATIO_90D,

                        curr.OVERFULL_VISIT_30D,
                        prev30.OVERFULL_VISIT_30D,

                        byTypeJsonField
                )
                .from(curr)
                .leftJoin(prev7).on(prev7.DATE_KEY.eq(previous7DateKey))
                .leftJoin(prev30).on(prev30.DATE_KEY.eq(previous30DateKey))
                .leftJoin(prev90).on(prev90.DATE_KEY.eq(previous90DateKey))
                .where(curr.DATE_KEY.eq(currentDateKey))
                .fetchOne();

        if (result == null) {
            return new DashboardRawData(
                    0L,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    "[]"
            );
        }

        return new DashboardRawData(
                longValue(result.get(curr.ACTIVE_BIN_COUNT)),

                decimalValue(result.get(curr.BIN_VISIT_COUNT_7D)),
                decimalValue(result.get(prev7.BIN_VISIT_COUNT_7D)),

                decimalValue(result.get(curr.EMPTIED_VISIT_COUNT_7D)),
                decimalValue(result.get(prev7.EMPTIED_VISIT_COUNT_7D)),

                decimalValue(result.get(curr.VISIT_EMPTIED_RATIO_7D)),
                decimalValue(result.get(prev7.VISIT_EMPTIED_RATIO_7D)),

                decimalValue(result.get(curr.LOW_FILL_VISIT_RATIO_90D)),
                decimalValue(result.get(prev90.LOW_FILL_VISIT_RATIO_90D)),

                decimalValue(result.get(curr.LOW_FILL_EMPTIED_RATIO_90D)),
                decimalValue(result.get(prev90.LOW_FILL_EMPTIED_RATIO_90D)),

                decimalValue(result.get(curr.OVERFULL_VISIT_30D)),
                decimalValue(result.get(prev30.OVERFULL_VISIT_30D)),

                result.get(byTypeJsonField)
        );
    }

    public Integer findLatestAvailableDateKey(int maxDateKeyInclusive) {
        return dsl
                .select(DSL.max(FACT_SYSTEM_DAY.DATE_KEY))
                .from(FACT_SYSTEM_DAY)
                .where(FACT_SYSTEM_DAY.DATE_KEY.le(maxDateKeyInclusive))
                .fetchOne(0, Integer.class);
    }

    private BigDecimal decimalValue(Number value) {
        return value != null ? BigDecimal.valueOf(value.doubleValue()) : BigDecimal.ZERO;
    }

    private long longValue(Number value) {
        return value != null ? value.longValue() : 0L;
    }
}