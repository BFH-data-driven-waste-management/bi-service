package ch.bfh.ddwm.dssbackend.toursoverview;

import ch.bfh.ddwm.dssbackend.common.model.VehicleEmptying;
import ch.bfh.ddwm.dssbackend.common.model.BinVisit;
import ch.bfh.ddwm.dssbackend.common.model.PageResult;
import ch.bfh.ddwm.dssbackend.common.repository.BinVisitRepository;
import ch.bfh.ddwm.dssbackend.common.repository.VehicleEmptyingRepository;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.*;
import ch.bfh.ddwm.dssbackend.toursoverview.model.*;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class TourOverviewRepository {

    private static final FactTour FACT_TOUR = Tables.FACT_TOUR;
    private static final DimVehicle DIM_VEHICLE = Tables.DIM_VEHICLE;

    private final DSLContext dsl;

    private final VehicleEmptyingRepository vehicleEmptyingRepository;
    private final BinVisitRepository binVisitRepository;

    public TourOverviewRepository(DSLContext dsl, VehicleEmptyingRepository vehicleEmptyingRepository, BinVisitRepository binVisitRepository) {
        this.dsl = dsl;
        this.vehicleEmptyingRepository = vehicleEmptyingRepository;
        this.binVisitRepository = binVisitRepository;
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

        return new PageResult<>(
                tours.isEmpty() ? Collections.emptyList() : buildTourOverviews(tours),
                page,
                size,
                totalElements);
    }


    public List<TourOverviewRow> findAllTours() {
        List<TourOverviewRow> tours = mapTourOverviewRows(tourOverviewBaseQueryOrderedBy(FACT_TOUR.STARTED_AT_TS.desc(), FACT_TOUR.TOUR_ID.desc()));


        return tours.isEmpty() ? Collections.emptyList() : tours;
    }

    private List<TourOverview> buildTourOverviews(List<TourOverviewRow> tours) {
        List<Long> tourIds = tours.stream()
                .map(TourOverviewRow::tourId)
                .toList();
        Map<Long, List<VehicleEmptying>> vehicleEmptyingsByTourId = vehicleEmptyingRepository.fetchVehicleEmptyingsByTour(tourIds);
        Map<Long, List<BinVisit>> binVisitsByTourId = binVisitRepository.fetchBinVisitsByTour(tourIds);

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
}
