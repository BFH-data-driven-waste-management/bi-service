package ch.bfh.ddwm.dssbackend.binlist;

import ch.bfh.ddwm.dssbackend.binlist.dto.BinListResponse;
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
        Integer latestFactBinDayDateKey = repository.findLatestFactBinDayDateKey();
        if (latestFactBinDayDateKey == null) {
            throw new IllegalStateException("No fact_bin_day snapshots available");
        }

        LocalDate latestDate = fromDateKey(latestFactBinDayDateKey);
        int startDateKey = toDateKey(latestDate.minusDays(89));

        return repository.findBinListByDateRange(startDateKey, latestFactBinDayDateKey).stream()
                .map(bin -> new BinListResponse(
                        bin.binKey(),
                        bin.type(),
                        bin.avgWeeklyVisits90d(),
                        bin.lowFillVisitRatio90d(),
                        bin.overfullVisitRatio90d()
                ))
                .toList();
    }

    private int toDateKey(LocalDate date) {
        return date.getYear() * 10_000
                + date.getMonthValue() * 100
                + date.getDayOfMonth();
    }

    private LocalDate fromDateKey(int dateKey) {
        int year = dateKey / 10_000;
        int month = (dateKey % 10_000) / 100;
        int day = dateKey % 100;
        return LocalDate.of(year, month, day);
    }
}
