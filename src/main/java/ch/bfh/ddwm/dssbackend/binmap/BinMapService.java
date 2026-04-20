package ch.bfh.ddwm.dssbackend.binmap;

import ch.bfh.ddwm.dssbackend.binmap.dto.BinMapResponse;
import ch.bfh.ddwm.dssbackend.binmap.model.BinMapItem;
import ch.bfh.ddwm.dssbackend.common.TodayDateProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.bfh.ddwm.dssbackend.common.DateKeyHelper.toDateKey;

@Service
public class BinMapService {

    private static final int MIN_HEAT = 0;
    private static final int MAX_HEAT = 99;
    private static final int HEAT_TIME_WINDOW_DAYS = 90;

    private final BinMapRepository repository;
    private final TodayDateProvider todayDateProvider;

    public BinMapService(BinMapRepository repository, TodayDateProvider todayDateProvider) {
        this.repository = repository;
        this.todayDateProvider = todayDateProvider;
    }

    public List<BinMapResponse> getBinMap() {
        LocalDate today = todayDateProvider.today();
        int todayDateKey = toDateKey(today);
        int windowStartDateKey = toDateKey(today.minusDays(HEAT_TIME_WINDOW_DAYS));

        List<BinMapItem> bins = repository.findBinMapByDateKey(todayDateKey);
        if (bins.isEmpty()) {
            throw new IllegalStateException("No bin map available");
        }

        Map<Long, BigDecimal> averageDailyAdditionByBinId = repository
                .findAvgDailyAdditionOverTimeWindowPerBin(todayDateKey, windowStartDateKey);

        Map<Long, Integer> wasteGenerationHeatByBinId = scaleHeatByBinId(
                bins,
                bin -> averageDailyAdditionByBinId.get(bin.binId())
        );

        Map<Long, Integer> lastEmptyingHeatByBinId = scaleHeatByBinId(
                bins,
                bin -> bin.isActive() ? toBigDecimal(bin.daysSinceLastEmptying()) : BigDecimal.ZERO
        );

        return bins.stream()
                .map(bin -> new BinMapResponse(
                        bin.binId(),
                        bin.type(),
                        bin.isActive(),
                        bin.coordX4326(),
                        bin.coordY4326(),
                        bin.coordX2056(),
                        bin.coordY2056(),
                        wasteGenerationHeatByBinId.getOrDefault(bin.binId(), MIN_HEAT),
                        lastEmptyingHeatByBinId.getOrDefault(bin.binId(), MIN_HEAT)
                ))
                .toList();
    }

    private Map<Long, Integer> scaleHeatByBinId(
            List<BinMapItem> bins,
            Function<BinMapItem, BigDecimal> valueExtractor
    ) {
        List<BigDecimal> values = bins.stream()
                .map(valueExtractor)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return Map.of();
        }

        BigDecimal min = values.stream()
                .min(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal max = values.stream()
                .max(BigDecimal::compareTo)
                .orElseThrow();

        return bins.stream()
                .collect(Collectors.toMap(
                        BinMapItem::binId,
                        bin -> scaleToHeat(valueExtractor.apply(bin), min, max)
                ));
    }

    private int scaleToHeat(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return MIN_HEAT;
        }

        if (min.compareTo(max) == 0) {
            return MAX_HEAT;
        }

        BigDecimal scaled = value.subtract(min)
                .multiply(BigDecimal.valueOf(MAX_HEAT))
                .divide(max.subtract(min), 0, RoundingMode.HALF_UP);

        return clampHeat(scaled.intValue());
    }

    private BigDecimal toBigDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value.longValue());
    }

    private int clampHeat(int value) {
        return Math.clamp(value, MIN_HEAT, MAX_HEAT);
    }
}