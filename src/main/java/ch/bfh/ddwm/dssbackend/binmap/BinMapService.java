package ch.bfh.ddwm.dssbackend.binmap;

import ch.bfh.ddwm.dssbackend.binmap.dto.BinMapResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BinMapService {

    private final BinMapRepository repository;

    public BinMapService(BinMapRepository repository) {
        this.repository = repository;
    }

    public List<BinMapResponse> getBinMap() {
        Integer latestFactBinDayDateKey = repository.findLatestFactBinDayDateKey();
        if (latestFactBinDayDateKey == null) {
            throw new IllegalStateException("No fact_bin_day snapshots available");
        }

        return repository.findBinMapByDateKey(latestFactBinDayDateKey).stream()
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
    }
}
