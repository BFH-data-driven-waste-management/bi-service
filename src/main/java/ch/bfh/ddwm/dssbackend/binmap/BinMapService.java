package ch.bfh.ddwm.dssbackend.binmap;

import ch.bfh.ddwm.dssbackend.binmap.dto.BinMapResponse;
import ch.bfh.ddwm.dssbackend.common.TodayDateProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static ch.bfh.ddwm.dssbackend.common.DateKeyHelper.toDateKey;

@Service
public class BinMapService {

    private final BinMapRepository repository;
    private final TodayDateProvider todayDateProvider;

    public BinMapService(BinMapRepository repository, TodayDateProvider todayDateProvider) {
        this.repository = repository;
        this.todayDateProvider = todayDateProvider;
    }

    public List<BinMapResponse> getBinMap() {
        LocalDate today = todayDateProvider.today();
        int todayDateKey = toDateKey(today);

        var result = repository.findBinMapByDateKey(todayDateKey).stream()
                .map(bin -> new BinMapResponse(
                        bin.binId(),
                        bin.type(),
                        bin.isActive(),
                        bin.coordX4326(),
                        bin.coordY4326(),
                        bin.coordX2056(),
                        bin.coordY2056()
                ))
                .toList();
        if (result.isEmpty()) {
            throw new IllegalStateException("No bin map available");
        }
        return result;
    }
}
