package ch.bfh.ddwm.dssbackend.tours;

import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimAction;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimBin;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimFillLevel;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.DimVehicle;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactBinVisit;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.FactTour;
import ch.bfh.ddwm.dssbackend.tours.model.BinVisit;
import ch.bfh.ddwm.dssbackend.tours.model.Tour;
import org.jooq.DSLContext;
import org.jooq.Record9;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TourRepository {

    private static final FactTour FACT_TOUR = Tables.FACT_TOUR;
    private static final DimVehicle DIM_VEHICLE = Tables.DIM_VEHICLE;
    private static final FactBinVisit FACT_BIN_VISIT = Tables.FACT_BIN_VISIT;
    private static final DimAction DIM_ACTION = Tables.DIM_ACTION;
    private static final DimFillLevel DIM_FILL_LEVEL = Tables.DIM_FILL_LEVEL;
    private static final DimBin DIM_BIN = Tables.DIM_BIN;

    private final DSLContext dsl;

    public TourRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PageResponse<Tour> findTours(int page, int size) {
        Long totalElementsValue = dsl.selectCount()
                .from(FACT_TOUR)
                .fetchOne(0, Long.class);
        long totalElements = totalElementsValue == null ? 0L : totalElementsValue;

        List<TourRow> tours = dsl.select(
                        FACT_TOUR.TOUR_ID,
                        DIM_VEHICLE.LICENSE_PLATE,
                        FACT_TOUR.STARTED_AT_TS,
                        FACT_TOUR.ENDED_AT_TS
                )
                .from(FACT_TOUR)
                .join(DIM_VEHICLE)
                .on(DIM_VEHICLE.VEHICLE_KEY.eq(FACT_TOUR.VEHICLE_KEY))
                .orderBy(FACT_TOUR.STARTED_AT_TS.desc(), FACT_TOUR.TOUR_ID.desc())
                .limit(size)
                .offset(page * size)
                .fetch(record -> new TourRow(
                        record.get(FACT_TOUR.TOUR_ID),
                        record.get(DIM_VEHICLE.LICENSE_PLATE),
                        record.get(FACT_TOUR.STARTED_AT_TS),
                        record.get(FACT_TOUR.ENDED_AT_TS)
                ));

        if (tours.isEmpty()) {
            int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
            return new PageResponse<>(List.of(), page, size, totalElements, totalPages);
        }

        List<Long> tourIds = tours.stream().map(TourRow::tourId).toList();
        Map<Long, List<BinVisit>> binVisitsByTourId = fetchBinVisitsByTour(tourIds);

        List<Tour> content = tours.stream()
                .map(tour -> new Tour(
                        tour.tourId(),
                        tour.licensePlate(),
                        tour.startedAt(),
                        tour.endedAt(),
                        binVisitsByTourId.getOrDefault(tour.tourId(), List.of())
                ))
                .toList();

        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    private Map<Long, List<BinVisit>> fetchBinVisitsByTour(List<Long> tourIds) {
        Map<Long, List<BinVisit>> binVisitsByTourId = new HashMap<>();

        for (Record9<Long, Long, Integer, OffsetDateTime, String, String, BigDecimal, BigDecimal, String> row : dsl.select(
                        FACT_BIN_VISIT.TOUR_ID,
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

    private record TourRow(
            long tourId,
            String licensePlate,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
    ) {}
}
