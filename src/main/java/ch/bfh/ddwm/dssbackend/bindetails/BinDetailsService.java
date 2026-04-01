package ch.bfh.ddwm.dssbackend.bindetails;

import ch.bfh.ddwm.dssbackend.bindetails.dto.BinDetailsResponse;
import ch.bfh.ddwm.dssbackend.bindetails.dto.BinFeatureSnapshotResponse;
import ch.bfh.ddwm.dssbackend.bindetails.model.BinDetails;
import org.springframework.stereotype.Service;

@Service
public class BinDetailsService {

    private final BinDetailsRepository repository;

    public BinDetailsService(BinDetailsRepository repository) {
        this.repository = repository;
    }

    public BinDetailsResponse getBinDetails(long binKey) {
        Integer latestFactBinDayDateKey = repository.findLatestBinDayFeaturesDateKey();
        if (latestFactBinDayDateKey == null) {
            throw new IllegalStateException("No fact_bin_day snapshots available");
        }

        BinDetails binDetails = repository.findBinDetailsByBinKeyAndDate(binKey, latestFactBinDayDateKey);
        if (binDetails == null) {
            throw new BinNotFoundException("No bin found for key " + binKey);
        }

        return new BinDetailsResponse(
                binDetails.binKey(),
                binDetails.type(),
                binDetails.volumeLiters(),
                binDetails.coordX2056(),
                binDetails.coordY2056(),
                binDetails.coordX4326(),
                binDetails.coordY4326(),
                new BinFeatureSnapshotResponse(
                        binDetails.featureSnapshot().baselineAvgVisitsPerWeek90d(),
                        binDetails.featureSnapshot().baselineAvgEmptyingsPerWeek90d(),
                        binDetails.featureSnapshot().lowFillVisitRatio90d(),
                        binDetails.featureSnapshot().notEmptiedRatio90d(),
                        binDetails.featureSnapshot().emptyingRank90d(),
                        binDetails.featureSnapshot().weatherSensitivityScore(),
                        binDetails.featureSnapshot().rainSensitivityScore(),
                        binDetails.featureSnapshot().sunSensitivityScore(),
                        binDetails.featureSnapshot().heatSensitivityScore(),
                        binDetails.featureSnapshot().eventSensitivityScore()
                )
        );
    }
}
