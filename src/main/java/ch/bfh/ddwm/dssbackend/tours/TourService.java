package ch.bfh.ddwm.dssbackend.tours;

import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.common.model.PageResult;
import ch.bfh.ddwm.dssbackend.tours.dto.BinVisitDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.TourDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.TourOverviewDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.VehicleEmptyingDTO;
import ch.bfh.ddwm.dssbackend.tours.model.Tour;
import ch.bfh.ddwm.dssbackend.tours.model.TourOverview;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TourService {
    private final TourRepository tourRepository;

    public TourService(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    public PageResponse<TourOverviewDTO> getTours(Pageable pageable) {
        int normalizedPage = Math.max(pageable.getPageNumber(), 0);
        int normalizedSize = Math.max(pageable.getPageSize(), 1);

        PageResult<TourOverview> tours = tourRepository.findTours(normalizedPage, normalizedSize);

        return new PageResponse<>(
                tours.content().stream()
                        .map(this::toTourOverviewDTO)
                        .toList(),
                tours.page(),
                tours.size(),
                tours.totalElements(),
                tours.totalPages()
        );
    }

    public TourDTO getTourById(long tourId) {
        Tour tour = tourRepository.findTourById(tourId);
        if (tour == null) {
            throw new IllegalStateException("No tour found for tour_id " + tourId);
        }

        return toTourDTO(tour);
    }

    private TourDTO toTourDTO(Tour tour) {
        return new TourDTO(
                tour.id(),
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
                tour.vehicleEmptyings().stream()
                        .map(vehicleEmptying -> new VehicleEmptyingDTO(
                                vehicleEmptying.id(),
                                vehicleEmptying.sequenceInTour(),
                                vehicleEmptying.eventTimestamp()
                        ))
                        .toList(),
                tour.binVisits().stream()
                        .map(binVisit -> new BinVisitDTO(
                                binVisit.id(),
                                binVisit.binId(),
                                binVisit.sequenceInTour(),
                                binVisit.eventTimestamp(),
                                binVisit.visitAction(),
                                binVisit.fillLevel(),
                                binVisit.binCoordX(),
                                binVisit.binCoordY(),
                                binVisit.binType()
                        ))
                        .toList()
        );
    }

    private TourOverviewDTO toTourOverviewDTO(TourOverview tourOverview) {
        return new TourOverviewDTO(
                tourOverview.id(),
                tourOverview.licensePlate(),
                tourOverview.vehicleEmptyingCount(),
                tourOverview.startedAt(),
                tourOverview.endedAt(),
                tourOverview.vehicleEmptyings().stream()
                        .map(vehicleEmptying -> new VehicleEmptyingDTO(
                                vehicleEmptying.id(),
                                vehicleEmptying.sequenceInTour(),
                                vehicleEmptying.eventTimestamp()
                        ))
                        .toList(),
                tourOverview.binVisits().stream()
                        .map(binVisit -> new BinVisitDTO(
                                binVisit.id(),
                                binVisit.binId(),
                                binVisit.sequenceInTour(),
                                binVisit.eventTimestamp(),
                                binVisit.visitAction(),
                                binVisit.fillLevel(),
                                binVisit.binCoordX(),
                                binVisit.binCoordY(),
                                binVisit.binType()
                        ))
                        .toList()
        );
    }
}
