package ch.bfh.ddwm.biservice.common.repository;

import ch.bfh.ddwm.biservice.common.model.BinVisit;
import ch.bfh.ddwm.biservice.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.biservice.jooq.generated.analytics.tables.*;
import org.jooq.DSLContext;
import org.jooq.Record10;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BinVisitRepository {

    private static final FactBinVisit FACT_BIN_VISIT = Tables.FACT_BIN_VISIT;
    private static final DimAction DIM_ACTION = Tables.DIM_ACTION;
    private static final DimFillLevel DIM_FILL_LEVEL = Tables.DIM_FILL_LEVEL;
    private static final DimBin DIM_BIN = Tables.DIM_BIN;

    private final DSLContext dsl;

    public BinVisitRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Map<Long, List<BinVisit>> fetchBinVisitsByTour(List<Long> tourIds) {
        Map<Long, List<BinVisit>> binVisitsByTourId = new HashMap<>();

        for (Record10<Long, Long, Long, Integer, OffsetDateTime, String, String, BigDecimal, BigDecimal, String> row : dsl.select(
                        FACT_BIN_VISIT.TOUR_ID,
                        DIM_BIN.BIN_ID,
                        FACT_BIN_VISIT.BIN_VISIT_ID,
                        FACT_BIN_VISIT.SEQUENCE_IN_TOUR,
                        FACT_BIN_VISIT.EVENT_TS,
                        DIM_ACTION.ACTION_CODE,
                        DIM_FILL_LEVEL.FILL_LEVEL_CODE,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326,
                        DIM_BIN.BIN_TYPE
                )
                .from(FACT_BIN_VISIT)
                .join(DIM_ACTION)
                .on(DIM_ACTION.ACTION_KEY.eq(FACT_BIN_VISIT.ACTION_KEY))
                .join(DIM_FILL_LEVEL)
                .on(DIM_FILL_LEVEL.FILL_LEVEL_KEY.eq(FACT_BIN_VISIT.FILL_LEVEL_KEY))
                .join(DIM_BIN)
                .on(DIM_BIN.BIN_KEY.eq(FACT_BIN_VISIT.BIN_KEY))
                .where(FACT_BIN_VISIT.TOUR_ID.in(tourIds))
                .orderBy(FACT_BIN_VISIT.TOUR_ID.asc(), FACT_BIN_VISIT.SEQUENCE_IN_TOUR.asc())
                .fetch()) {
            Long tourId = row.get(FACT_BIN_VISIT.TOUR_ID);
            BinVisit visit = new BinVisit(
                    row.get(FACT_BIN_VISIT.BIN_VISIT_ID),
                    row.get(DIM_BIN.BIN_ID),
                    row.get(FACT_BIN_VISIT.SEQUENCE_IN_TOUR),
                    row.get(FACT_BIN_VISIT.EVENT_TS),
                    row.get(DIM_ACTION.ACTION_CODE),
                    row.get(DIM_FILL_LEVEL.FILL_LEVEL_CODE),
                    row.get(DIM_BIN.COORD_X_4326),
                    row.get(DIM_BIN.COORD_Y_4326),
                    row.get(DIM_BIN.BIN_TYPE)
            );

            binVisitsByTourId
                    .computeIfAbsent(tourId, ignored -> new ArrayList<>())
                    .add(visit);
        }

        return binVisitsByTourId;
    }
}
