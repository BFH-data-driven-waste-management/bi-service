package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDetailsResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDayFeaturesResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.DailyCountResponse;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static ch.bfh.ddwm.dssbackend.common.DateKeyHelper.toDateKey;
import static ch.bfh.ddwm.dssbackend.common.KpiMetricCalculator.buildDecimalMetric;

@Service
public class BinDetailsService {

    private final BinDetailsRepository repository;

    public BinDetailsService(BinDetailsRepository repository) {
        this.repository = repository;
    }

    public BinDetailsResponse getBinDetails(long binId) {
        LocalDate today = LocalDate.now();
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
                                previousFeatureSnapshot.map(features -> features.baselineAvgVisitsPerWeek90d()).orElse(null)
                        ),
                        buildDecimalMetric(
                                binDetails.featureSnapshot().baselineAvgEmptyingsPerWeek90d(),
                                previousFeatureSnapshot.map(features -> features.baselineAvgEmptyingsPerWeek90d()).orElse(null)
                        ),
                        binDetails.featureSnapshot().lowFillVisitRatio90d(),
                        binDetails.featureSnapshot().notEmptiedRatio90d(),
                        binDetails.featureSnapshot().emptyingRank90d(),
                        binDetails.featureSnapshot().weatherSensitivityScore(),
                        binDetails.featureSnapshot().rainSensitivityScore(),
                        binDetails.featureSnapshot().sunSensitivityScore(),
                        binDetails.featureSnapshot().heatSensitivityScore(),
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
}
