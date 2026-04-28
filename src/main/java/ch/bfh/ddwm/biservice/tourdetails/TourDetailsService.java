package ch.bfh.ddwm.biservice.tourdetails;

import ch.bfh.ddwm.biservice.common.api.ResourceNotFoundException;
import ch.bfh.ddwm.biservice.common.dto.BinVisit;
import ch.bfh.ddwm.biservice.tourdetails.dto.TourResponse;
import ch.bfh.ddwm.biservice.common.dto.VehicleEmptying;
import ch.bfh.ddwm.biservice.tourdetails.model.Tour;
import org.springframework.stereotype.Service;

@Service
public class TourDetailsService {
    private final TourDetailsRepository tourDetailsRepository;

    public TourDetailsService(TourDetailsRepository tourDetailsRepository) {
        this.tourDetailsRepository = tourDetailsRepository;
    }


    public TourResponse getTourById(long tourId) {
        Tour tour = tourDetailsRepository.findTourById(tourId);
        if (tour == null) {
            throw new ResourceNotFoundException("No tour found for id " + tourId);
        }

        return toTourResponse(tour);
    }

    private TourResponse toTourResponse(Tour tour) {
        return new TourResponse(
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
                        .map(vehicleEmptying -> new VehicleEmptying(
                                vehicleEmptying.id(),
                                vehicleEmptying.sequenceInTour(),
                                vehicleEmptying.eventTimestamp()
                        ))
                        .toList(),
                tour.binVisits().stream()
                        .map(binVisit -> new BinVisit(
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
