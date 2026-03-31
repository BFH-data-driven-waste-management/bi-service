package ch.bfh.ddwm.dssbackend.binmap;

import ch.bfh.ddwm.dssbackend.binmap.model.BinMapItem;
import ch.bfh.ddwm.dssbackend.jooq.generated.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.tables.FactBinDay;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BinMapRepository {

    private static final DimBin DIM_BIN = Tables.DIM_BIN;
    private static final FactBinDay FACT_BIN_DAY = Tables.FACT_BIN_DAY;

    private final DSLContext dsl;

    public BinMapRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Integer findLatestFactBinDayDateKey() {
        return dsl
                .select(DSL.max(FACT_BIN_DAY.DATE_KEY))
                .from(FACT_BIN_DAY)
                .fetchOne(0, Integer.class);
    }

    public List<BinMapItem> findBinMapByDateKey(int dateKey) {
        return dsl
                .select(
                        FACT_BIN_DAY.BIN_KEY,
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.CURRENT_ACTIVE_FLAG,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326
                )
                .from(FACT_BIN_DAY)
                .join(DIM_BIN).on(DIM_BIN.BIN_ID.eq(FACT_BIN_DAY.BIN_KEY))
                .where(FACT_BIN_DAY.DATE_KEY.eq(dateKey))
                .orderBy(FACT_BIN_DAY.BIN_KEY.asc())
                .fetch(record -> new BinMapItem(
                        record.get(FACT_BIN_DAY.BIN_KEY),
                        record.get(DIM_BIN.BIN_TYPE),
                        Boolean.TRUE.equals(record.get(DIM_BIN.CURRENT_ACTIVE_FLAG)),
                        record.get(DIM_BIN.COORD_X_4326),
                        record.get(DIM_BIN.COORD_Y_4326)
                ));
    }
}
