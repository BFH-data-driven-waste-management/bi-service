package ch.bfh.ddwm.dssbackend.common.repository;

import ch.bfh.ddwm.dssbackend.common.model.VehicleEmptying;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactVehicleEmptying;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VehicleEmptyingRepository {

    private static final FactVehicleEmptying FACT_VEHICLE_EMPTYING = Tables.FACT_VEHICLE_EMPTYING;

    private final DSLContext dsl;

    public VehicleEmptyingRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Map<Long, List<VehicleEmptying>> fetchVehicleEmptyingsByTour(List<Long> tourIds) {
        Map<Long, List<VehicleEmptying>> vehicleEmptyingsByTourId = new HashMap<>();

        for (Record4<Long, Long, Integer, OffsetDateTime> row : dsl.select(
                        FACT_VEHICLE_EMPTYING.TOUR_ID,
                        FACT_VEHICLE_EMPTYING.VEHICLE_EMPTYING_KEY,
                        FACT_VEHICLE_EMPTYING.SEQUENCE_IN_TOUR,
                        FACT_VEHICLE_EMPTYING.EVENT_TS
                )
                .from(FACT_VEHICLE_EMPTYING)
                .where(FACT_VEHICLE_EMPTYING.TOUR_ID.in(tourIds))
                .orderBy(FACT_VEHICLE_EMPTYING.TOUR_ID.asc(), FACT_VEHICLE_EMPTYING.SEQUENCE_IN_TOUR.asc())
                .fetch()) {
            Long tourId = row.get(FACT_VEHICLE_EMPTYING.TOUR_ID);
            VehicleEmptying vehicleEmptying = new VehicleEmptying(
                    row.get(FACT_VEHICLE_EMPTYING.VEHICLE_EMPTYING_KEY),
                    row.get(FACT_VEHICLE_EMPTYING.SEQUENCE_IN_TOUR),
                    row.get(FACT_VEHICLE_EMPTYING.EVENT_TS)
            );

            vehicleEmptyingsByTourId
                    .computeIfAbsent(tourId, ignored -> new ArrayList<>())
                    .add(vehicleEmptying);
        }

        return vehicleEmptyingsByTourId;
    }
}
