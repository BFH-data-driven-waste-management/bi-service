package ch.bfh.ddwm.dssbackend.tourdetails;

import ch.bfh.ddwm.dssbackend.common.repository.BinVisitRepository;
import ch.bfh.ddwm.dssbackend.common.repository.VehicleEmptyingRepository;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.Tables;
import ch.bfh.ddwm.dssbackend.jooq.generated.analytics.tables.*;
import ch.bfh.ddwm.dssbackend.common.model.BinVisit;
import ch.bfh.ddwm.dssbackend.tourdetails.model.Tour;
import ch.bfh.ddwm.dssbackend.tourdetails.model.TourRow;
import ch.bfh.ddwm.dssbackend.common.model.VehicleEmptying;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class TourDetailsRepository {

    private static final FactTour FACT_TOUR = Tables.FACT_TOUR;
    private static final DimVehicle DIM_VEHICLE = Tables.DIM_VEHICLE;

    private final DSLContext dsl;

    private final VehicleEmptyingRepository vehicleEmptyingRepository;
    private final BinVisitRepository binVisitRepository;

    public TourDetailsRepository(DSLContext dsl, VehicleEmptyingRepository vehicleEmptyingRepository, BinVisitRepository binVisitRepository) {
        this.dsl = dsl;
        this.vehicleEmptyingRepository = vehicleEmptyingRepository;
        this.binVisitRepository = binVisitRepository;
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
        Map<Long, List<VehicleEmptying>> vehicleEmptyingsByTourId = vehicleEmptyingRepository.fetchVehicleEmptyingsByTour(tourIds);
        Map<Long, List<BinVisit>> binVisitsByTourId = binVisitRepository.fetchBinVisitsByTour(tourIds);

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
}
