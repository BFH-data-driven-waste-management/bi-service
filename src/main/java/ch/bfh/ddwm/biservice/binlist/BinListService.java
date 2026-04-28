package ch.bfh.ddwm.biservice.binlist;

import ch.bfh.ddwm.biservice.binlist.dto.BinListResponse;
import ch.bfh.ddwm.biservice.common.TodayDateProvider;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.bfh.ddwm.biservice.common.DateKeyHelper.toDateKey;

@Service
public class BinListService {

    private final BinListRepository repository;
    private final TodayDateProvider todayDateProvider;

    public BinListService(BinListRepository repository, TodayDateProvider todayDateProvider) {
        this.repository = repository;
        this.todayDateProvider = todayDateProvider;
    }

    public List<BinListResponse> getBinList() {
        int todayDateKey = toDateKey(todayDateProvider.today());

        return repository.findBinListByDateKey(todayDateKey).stream()
                .map(bin -> new BinListResponse(
                        bin.binId(),
                        bin.type(),
                        bin.isActive(),
                        bin.avgWeeklyVisits90d(),
                        bin.lowFillVisitRatio90d(),
                        bin.overfullVisitRatio90d(),
                        bin.coordX2056(),
                        bin.coordY2056()
                ))
                .toList();
    }
}
