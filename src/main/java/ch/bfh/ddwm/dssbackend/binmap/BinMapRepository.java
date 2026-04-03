package ch.bfh.ddwm.dssbackend.binmap;

import ch.bfh.ddwm.dssbackend.binmap.model.BinMapItem;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactBinDailySnapshot;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BinMapRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final FactBinDailySnapshot FACT_BIN_DAILY_SNAPSHOT = Tables.FACT_BIN_DAILY_SNAPSHOT;

    private final DSLContext dsl;

    public BinMapRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Integer findLatestFactBinDayDateKey() {
        return dsl
                .select(DSL.max(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY))
                .from(FACT_BIN_DAILY_SNAPSHOT)
                .fetchOne(0, Integer.class);
    }

    public List<BinMapItem> findBinMapByDateKey(int dateKey) {
        return dsl
                .select(
                        DIM_BIN.BIN_ID,
                        FACT_BIN_DAILY_SNAPSHOT.BIN_KEY,
                        DIM_BIN.BIN_TYPE,
                        FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056
                )
                .from(FACT_BIN_DAILY_SNAPSHOT)
                .join(DIM_BIN).on(DIM_BIN.BIN_ID.eq(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY))
                .where(FACT_BIN_DAILY_SNAPSHOT.DATE_KEY.eq(dateKey))
                .orderBy(FACT_BIN_DAILY_SNAPSHOT.BIN_KEY.asc())
                .fetch(record -> new BinMapItem(
                        record.get(DIM_BIN.BIN_ID),
                        record.get(DIM_BIN.BIN_TYPE),
                        record.get(FACT_BIN_DAILY_SNAPSHOT.IS_ACTIVE),
                        record.get(DIM_BIN.COORD_X_4326),
                        record.get(DIM_BIN.COORD_Y_4326),
                        record.get(DIM_BIN.COORD_X_2056),
                        record.get(DIM_BIN.COORD_Y_2056)
                ));
    }
}
