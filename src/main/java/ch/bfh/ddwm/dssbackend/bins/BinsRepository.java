package ch.bfh.ddwm.dssbackend.bins;

import ch.bfh.ddwm.dssbackend.bins.dto.BinResponse;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ch.bfh.ddwm.dssbackend.jooq.generated.tables.DimBin.DIM_BIN;

@Repository
public class BinsRepository {

    private final DSLContext dsl;

    public BinsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<BinResponse> findAllBins() {
        return dsl
                .select(
                        DIM_BIN.BIN_TYPE,
                        DIM_BIN.CURRENT_ACTIVE_FLAG,
                        DIM_BIN.COORD_X_2056,
                        DIM_BIN.COORD_Y_2056,
                        DIM_BIN.COORD_X_4326,
                        DIM_BIN.COORD_Y_4326
                )
                .from(DIM_BIN)
                .orderBy(DIM_BIN.BIN_ID.asc())
                .fetch(result -> new BinResponse(
                        result.get(DIM_BIN.BIN_TYPE),
                        result.get(DIM_BIN.CURRENT_ACTIVE_FLAG),
                        result.get(DIM_BIN.COORD_X_2056),
                        result.get(DIM_BIN.COORD_Y_2056),
                        result.get(DIM_BIN.COORD_X_4326),
                        result.get(DIM_BIN.COORD_Y_4326)
                ));
    }
}