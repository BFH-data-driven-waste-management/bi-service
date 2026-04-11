package ch.bfh.ddwm.dssbackend.tours;

import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.common.model.PageResult;
import ch.bfh.ddwm.dssbackend.tours.dto.BinVisitDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.TourDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.TourOverviewDTO;
import ch.bfh.ddwm.dssbackend.tours.dto.VehicleEmptyingDTO;
import ch.bfh.ddwm.dssbackend.tours.model.Tour;
import ch.bfh.ddwm.dssbackend.tours.model.TourOverview;
import ch.bfh.ddwm.dssbackend.tours.model.TourOverviewRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;

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

    public String getToursCsv() {
        var tours = tourRepository.findAllTours();

        try (StringWriter out = new StringWriter();
             CSVPrinter printer = new CSVPrinter(
                     out,
                     CSVFormat.DEFAULT.builder()
                             .setHeader(
                                     "ID",
                                     "Fahrzeug",
                                     "Gestartet am",
                                     "Beendet am",
                                     "Anzahl Behälterbesuche"
                             ).get()
             )) {
            for (TourOverviewRow tour : tours) {
                printer.printRecord(
                        tour.tourId(),
                        tour.licensePlate(),
                        tour.startedAt(),
                        tour.endedAt(),
                        tour.vehicleEmptyingCount()
                );
            }
            printer.flush();
            return out.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create CSV export for tours", e);
        }
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
