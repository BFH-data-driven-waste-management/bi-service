package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDetailsResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDayFeaturesResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.BinVisitHistoryResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.DailyCountResponse;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinVisitHistory;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinDayFeatures;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinDetails;
import ch.bfh.ddwm.dssbackend.common.api.PageResponse;
import ch.bfh.ddwm.dssbackend.common.model.PageResult;
import ch.bfh.ddwm.dssbackend.common.TodayDateProvider;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

import static ch.bfh.ddwm.dssbackend.common.DateKeyHelper.toDateKey;
import static ch.bfh.ddwm.dssbackend.common.KpiMetricCalculator.buildDecimalMetric;

@Service
public class BinDetailsService {

    private final BinDetailsRepository repository;
    private final TodayDateProvider todayDateProvider;

    public BinDetailsService(BinDetailsRepository repository, TodayDateProvider todayDateProvider) {
        this.repository = repository;
        this.todayDateProvider = todayDateProvider;
    }

    public BinDetailsResponse getBinDetails(long binId) {
        LocalDate today = todayDateProvider.today();
        int todayDateKey = toDateKey(today);
        int lastWeekDateKey = toDateKey(today.minusDays(7));

        BinDetails binDetails = repository.binDayFeaturesByBinIdAndDateKey(binId, todayDateKey);
        if (binDetails == null) {
            throw new IllegalStateException("No bin details found for bin " + binId + " at date_key " + todayDateKey);
        }

        int todayMinus90dDateKey = toDateKey(today.minusDays(90));
        int todayMinus12mDateKey = toDateKey(today.minusDays(12 * 30));
        var previousFeatureSnapshot = repository.findFeatureSnapshotByBinIdAndDateKey(binId, lastWeekDateKey);

        return new BinDetailsResponse(
                binDetails.binId(),
                binDetails.binType(),
                binDetails.volumeLiters(),
                binDetails.active(),
                binDetails.coordX2056(),
                binDetails.coordY2056(),
                binDetails.coordX4326(),
                binDetails.coordY4326(),
                binDetails.lastVisitDateKey(),
                binDetails.lastEmptyingDateKey(),
                new BinDayFeaturesResponse(
                        buildDecimalMetric(
                                binDetails.featureSnapshot().baselineAvgVisitsPerWeek90d(),
                                previousFeatureSnapshot.map(BinDayFeatures::baselineAvgVisitsPerWeek90d).orElse(null)
                        ),
                        buildDecimalMetric(
                                binDetails.featureSnapshot().baselineAvgEmptyingsPerWeek90d(),
                                previousFeatureSnapshot.map(BinDayFeatures::baselineAvgEmptyingsPerWeek90d).orElse(null)
                        ),
                        binDetails.featureSnapshot().lowFillVisitRatio90d(),
                        binDetails.featureSnapshot().overfullVisitRatio90d(),
                        binDetails.featureSnapshot().notEmptiedRatio90d(),
                        binDetails.featureSnapshot().emptyingRank90d(),
                        binDetails.featureSnapshot().goodWeatherSensitivityScore(),
                        binDetails.featureSnapshot().badWeatherSensitivityScore(),
                        binDetails.featureSnapshot().eventSensitivityScore()
                ),
                repository.findVisitFrequencyPerWeekInWindow(binId, todayMinus90dDateKey, todayDateKey).stream()
                        .map(point -> new DailyCountResponse(point.dateKey(), point.count()))
                        .toList(),
                repository.findEmptyingFrequencyPerWeekInWindow(binId, todayMinus90dDateKey, todayDateKey).stream()
                        .map(point -> new DailyCountResponse(point.dateKey(), point.count()))
                        .toList(),
                repository.findFillTrend12m(binId, todayMinus12mDateKey, todayDateKey).stream()
                        .map(point -> new DailyCountResponse(point.dateKey(), point.count()))
                        .toList()
        );
    }

    public PageResponse<BinVisitHistoryResponse> getBinVisits(long binId, Pageable pageable) {
        int normalizedPage = Math.max(pageable.getPageNumber(), 0);
        int normalizedSize = Math.max(pageable.getPageSize(), 1);

        PageResult<BinVisitHistory> binVisits = repository.findBinVisitsByBinId(binId, normalizedPage, normalizedSize);

        return new PageResponse<>(
                binVisits.content().stream()
                        .map(this::toBinVisitHistoryResponse)
                        .toList(),
                binVisits.page(),
                binVisits.size(),
                binVisits.totalElements(),
                binVisits.totalPages()
        );
    }

    public String getBinVisitsCsv(long binId) {
        List<BinVisitHistory> visits = repository.findAllBinVisitsByBinId(binId);

        try (StringWriter out = new StringWriter();
             CSVPrinter printer = new CSVPrinter(
                     out,
                     CSVFormat.DEFAULT.builder()
                             .setHeader(
                                     "Besuchs-ID",
                                     "Tour-ID",
                                     "Position in Tour",
                                     "Zeitpunkt",
                                     "Fahrzeug",
                                     "Füllstand",
                                     "Aktion"
                             ).get()
             )) {
            for (BinVisitHistory visit : visits) {
                printer.printRecord(
                        visit.binVisitId(),
                        visit.tourId(),
                        visit.sequenceInTour(),
                        visit.eventTimestamp(),
                        visit.licensePlate(),
                        visit.fillLevelCode(),
                        visit.actionCode()
                );
            }
            printer.flush();
            return out.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create CSV export for bin " + binId, e);
        }
    }

    private BinVisitHistoryResponse toBinVisitHistoryResponse(BinVisitHistory binVisitHistory) {
        return new BinVisitHistoryResponse(
                binVisitHistory.binVisitId(),
                binVisitHistory.binId(),
                binVisitHistory.tourId(),
                binVisitHistory.sequenceInTour(),
                binVisitHistory.eventTimestamp(),
                binVisitHistory.fillLevelCode(),
                binVisitHistory.actionCode(),
                binVisitHistory.licensePlate()
        );
    }
}
