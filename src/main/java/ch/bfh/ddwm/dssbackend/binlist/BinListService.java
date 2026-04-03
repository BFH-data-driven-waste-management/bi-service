package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.binlist.dto.BinListResponse;
import ch.bfh.ddwm.dssbackend.common.DateKeyHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BinListService {

    private final BinListRepository repository;

    public BinListService(BinListRepository repository) {
        this.repository = repository;
    }

    public List<BinListResponse> getBinList() {
        Integer latestFactBinDayDateKey = repository.findLatestBinDayFeaturesDateKey();
        if (latestFactBinDayDateKey == null) {
            throw new IllegalStateException("No fact_bin_day snapshots available");
        }

        LocalDate latestDate = DateKeyHelper.fromDateKey(latestFactBinDayDateKey);
        int startDateKey = DateKeyHelper.toDateKey(latestDate.minusDays(89));

        return repository.findBinListByDateRange(startDateKey, latestFactBinDayDateKey).stream()
                .map(bin -> new BinListResponse(
                        bin.type(),
                        bin.isActive(),
                        bin.avgWeeklyVisits90d(),
                        bin.lowFillVisitRatio90d(),
                        bin.overfullVisitRatio90d()
                ))
                .toList();
    }
}
