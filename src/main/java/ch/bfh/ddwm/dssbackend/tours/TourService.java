package ch.bfh.ddwm.dssbackend.tours;

import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.common.model.PageResult;
import ch.bfh.ddwm.dssbackend.tours.dto.BinVisitDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.TourDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.VehicleEmptyingDTO;
import ch.bfh.ddwm.dssbackend.tours.model.Tour;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TourService {
    private final TourRepository tourRepository;

    public TourService(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    public PageResponse<TourDTO> getTours(Pageable pageable) {
        int normalizedPage = Math.max(pageable.getPageNumber(), 0);
        int normalizedSize = Math.max(pageable.getPageSize(), 1);

        PageResult<Tour> tours = tourRepository.findTours(normalizedPage, normalizedSize);

        return new PageResponse<>(
                tours.content().stream()
                        .map(tour -> new TourDTO(
                                tour.id(),
                                tour.licensePlate(),
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
                                                binVisit.sequenceInTour(),
                                                binVisit.eventTimestamp(),
                                                binVisit.visitAction(),
                                                binVisit.fillLevel(),
                                                binVisit.binCoordX(),
                                                binVisit.binCoordY(),
                                                binVisit.binType()
                                        ))
                                        .toList()
                        ))
                        .toList(),
                tours.page(),
                tours.size(),
                tours.totalElements(),
                tours.totalPages()
        );
    }
}
