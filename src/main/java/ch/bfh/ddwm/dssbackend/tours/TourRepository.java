package ch.bfh.ddwm.dssbackend.tours;

import ch.bfh.ddwm.dssbackend.common.model.PageResult;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.*;
import ch.bfh.ddwm.dssbackend.tours.model.*;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class TourRepository {

    private static final FactTour FACT_TOUR = Tables.FACT_TOUR;
    private static final DimVehicle DIM_VEHICLE = Tables.DIM_VEHICLE;
    private static final FactBinVisit FACT_BIN_VISIT = Tables.FACT_BIN_VISIT;
    private static final FactVehicleEmptying FACT_VEHICLE_EMPTYING = Tables.FACT_VEHICLE_EMPTYING;
    private static final DimAction DIM_ACTION = Tables.DIM_ACTION;
    private static final DimFillLevel DIM_FILL_LEVEL = Tables.DIM_FILL_LEVEL;
    private static final DimBin DIM_BIN = Tables.DIM_BIN;

    private final DSLContext dsl;

    public TourRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PageResult<TourOverview> findTours(int page, int size) {
        Long totalElementsValue = dsl.selectCount()
                .from(FACT_TOUR)
                .fetchOne(0, Long.class);
        long totalElements = totalElementsValue == null ? 0L : totalElementsValue;

        List<TourOverviewRow> tours = mapTourOverviewRows(
                tourOverviewBaseQueryOrderedBy(FACT_TOUR.STARTED_AT_TS.desc(), FACT_TOUR.TOUR_ID.desc())
                        .limit(size)
                        .offset(page * size)
        );

        if (tours.isEmpty()) {
            int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
            return new PageResult<>(List.of(), page, size, totalElements, totalPages);
        }

        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(buildTourOverviews(tours), page, size, totalElements, totalPages);
    }


    public List<TourOverviewRow> findAllTours() {
        List<TourOverviewRow> tours = mapTourOverviewRows(tourOverviewBaseQueryOrderedBy(FACT_TOUR.TOUR_ID.asc())); // TODO is sorting by latest (like in bin visits export) better?


        return tours.isEmpty() ? Collections.emptyList() : tours;
    }


    public Tour findTourById(long tourId) {
        List<TourRow> tours = dsl.select(
                        FACT_TOUR.TOUR_ID,
                        DIM_VEHICLE.LICENSE_PLATE,
                        FACT_TOUR.VISIT_COUNT,
                        FACT_TOUR.EMPTIED_VISIT_COUNT,
                        FACT_TOUR.NOT_EMPTIED_VISIT_COUNT,
                        FACT_TOUR.LOW_FILL_VISIT_COUNT,
                        FACT_TOUR.HIGH_FILL_VISIT_COUNT,
                        FACT_TOUR.OVERFULL_VISIT_COUNT,
                        FACT_TOUR.VEHICLE_EMPTYING_COUNT,
                        FACT_TOUR.STARTED_AT_TS,
                        FACT_TOUR.ENDED_AT_TS
                )
                .from(FACT_TOUR)
                .join(DIM_VEHICLE)
                .on(DIM_VEHICLE.VEHICLE_KEY.eq(FACT_TOUR.VEHICLE_KEY))
                .where(FACT_TOUR.TOUR_ID.eq(tourId))
                .fetch(record -> new TourRow(
                        record.get(FACT_TOUR.TOUR_ID),
                        record.get(DIM_VEHICLE.LICENSE_PLATE),
                        record.get(FACT_TOUR.VISIT_COUNT),
                        record.get(FACT_TOUR.EMPTIED_VISIT_COUNT),
                        record.get(FACT_TOUR.NOT_EMPTIED_VISIT_COUNT),
                        record.get(FACT_TOUR.LOW_FILL_VISIT_COUNT),
                        record.get(FACT_TOUR.HIGH_FILL_VISIT_COUNT),
                        record.get(FACT_TOUR.OVERFULL_VISIT_COUNT),
                        record.get(FACT_TOUR.VEHICLE_EMPTYING_COUNT),
                        record.get(FACT_TOUR.STARTED_AT_TS),
                        record.get(FACT_TOUR.ENDED_AT_TS)
                ));

        if (tours.isEmpty()) {
            return null;
        }

        return buildTours(tours).getFirst();
    }

    private List<Tour> buildTours(List<TourRow> tours) {
        List<Long> tourIds = tours.stream().map(TourRow::tourId).toList();
        Map<Long, List<VehicleEmptying>> vehicleEmptyingsByTourId = fetchVehicleEmptyingsByTour(tourIds);
        Map<Long, List<BinVisit>> binVisitsByTourId = fetchBinVisitsByTour(tourIds);

        return tours.stream()
                .map(tour -> new Tour(
                        tour.tourId(),
                        tour.licensePlate(),
                        tour.visitCount(),
                        tour.emptiedVisitCount(),
                        tour.notEmptiedVisitCount(),
                        tour.lowFillVisitCount(),
                        tour.highFillVisitCount(),
                        tour.overfullVisitCount(),
                        tour.vehicleEmptyingCount(),
                        tour.startedAt(),
                        tour.endedAt(),
                        vehicleEmptyingsByTourId.getOrDefault(tour.tourId(), List.of()),
                        binVisitsByTourId.getOrDefault(tour.tourId(), List.of())
                ))
                .toList();
    }

    private List<TourOverview> buildTourOverviews(List<TourOverviewRow> tours) {
        List<Long> tourIds = tours.stream()
                .map(TourOverviewRow::tourId)
                .toList();
        Map<Long, List<VehicleEmptying>> vehicleEmptyingsByTourId = fetchVehicleEmptyingsByTour(tourIds);
        Map<Long, List<BinVisit>> binVisitsByTourId = fetchBinVisitsByTour(tourIds);

        return tours.stream()
                .map(tour -> new TourOverview(
                        tour.tourId(),
                        tour.licensePlate(),
                        tour.vehicleEmptyingCount(),
                        tour.startedAt(),
                        tour.endedAt(),
                        vehicleEmptyingsByTourId.getOrDefault(tour.tourId(), List.of()),
                        binVisitsByTourId.getOrDefault(tour.tourId(), List.of())
                ))
                .toList();
    }

    private SelectLimitStep<Record5<Long, String, Integer, OffsetDateTime, OffsetDateTime>> tourOverviewBaseQueryOrderedBy(SortField<?>... sortFields) {
        return dsl.select(
                        FACT_TOUR.TOUR_ID,
                        DIM_VEHICLE.LICENSE_PLATE,
                        FACT_TOUR.VEHICLE_EMPTYING_COUNT,
                        FACT_TOUR.STARTED_AT_TS,
                        FACT_TOUR.ENDED_AT_TS
                )
                .from(FACT_TOUR)
                .join(DIM_VEHICLE)
                .on(DIM_VEHICLE.VEHICLE_KEY.eq(FACT_TOUR.VEHICLE_KEY))
                .orderBy(sortFields);
    }

    private List<TourOverviewRow> mapTourOverviewRows(ResultQuery<Record5<Long, String, Integer, OffsetDateTime, OffsetDateTime>> query) {
        return query.fetch(record -> new TourOverviewRow(
                record.get(FACT_TOUR.TOUR_ID),
                record.get(DIM_VEHICLE.LICENSE_PLATE),
                record.get(FACT_TOUR.VEHICLE_EMPTYING_COUNT),
                record.get(FACT_TOUR.STARTED_AT_TS),
                record.get(FACT_TOUR.ENDED_AT_TS)
        ));
    }

    private Map<Long, List<VehicleEmptying>> fetchVehicleEmptyingsByTour(List<Long> tourIds) {
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

    private Map<Long, List<BinVisit>> fetchBinVisitsByTour(List<Long> tourIds) {
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
