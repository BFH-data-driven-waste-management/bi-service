package ch.bfh.ddwm.dssbackend.toursoverview;

import ch.bfh.ddwm.dssbackend.common.api.CsvExportException;
import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.common.dto.VehicleEmptying;
import ch.bfh.ddwm.dssbackend.common.dto.BinVisit;
import ch.bfh.ddwm.dssbackend.common.model.PageResult;
import ch.bfh.ddwm.dssbackend.toursoverview.dto.TourOverviewResponse;
import ch.bfh.ddwm.dssbackend.toursoverview.model.TourOverview;
import ch.bfh.ddwm.dssbackend.toursoverview.model.TourOverviewRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;

@Service
public class TourOverviewService {
    private final TourOverviewRepository tourOverviewRepository;

    public TourOverviewService(TourOverviewRepository tourOverviewRepository) {
        this.tourOverviewRepository = tourOverviewRepository;
    }

    public PageResponse<TourOverviewResponse> getTours(Pageable pageable) {
        int normalizedPage = Math.max(pageable.getPageNumber(), 0);
        int normalizedSize = Math.max(pageable.getPageSize(), 1);

        PageResult<TourOverview> tours = tourOverviewRepository.findTours(normalizedPage, normalizedSize);

        return new PageResponse<>(
                tours.content().stream()
                        .map(this::toTourOverviewResponse)
                        .toList(),
                tours.page(),
                tours.size(),
                tours.totalElements()
        );
    }

    public String getToursCsv() {
        var tours = tourOverviewRepository.findAllTours();

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
            throw new CsvExportException("Failed to create CSV export for tours", e);
        }
    }



    private TourOverviewResponse toTourOverviewResponse(TourOverview tourOverview) {
        return new TourOverviewResponse(
                tourOverview.id(),
                tourOverview.licensePlate(),
                tourOverview.vehicleEmptyingCount(),
                tourOverview.startedAt(),
                tourOverview.endedAt(),
                tourOverview.vehicleEmptyings().stream()
                        .map(vehicleEmptying -> new VehicleEmptying(
                                vehicleEmptying.id(),
                                vehicleEmptying.sequenceInTour(),
                                vehicleEmptying.eventTimestamp()
                        ))
                        .toList(),
                tourOverview.binVisits().stream()
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
