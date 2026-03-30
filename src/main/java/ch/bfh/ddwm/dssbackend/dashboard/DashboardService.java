package ch.bfh.ddwm.dssbackend.dashboard;

import ch.bfh.ddwm.dssbackend.dashboard.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class DashboardService {

    private static final int KPI_SCALE = 4;

    private final DashboardRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        int todayDateKey = toDateKey(today);

        Integer currentDateKey = repository.findLatestAvailableDateKey(todayDateKey);
        if (currentDateKey == null) {
            throw new IllegalStateException("No fact_system_day snapshot available up to today");
        }

        LocalDate referenceDate = fromDateKey(currentDateKey);

        int previous7DateKey = toDateKey(referenceDate.minusDays(7));
        int previous30DateKey = toDateKey(referenceDate.minusDays(30));
        int previous90DateKey = toDateKey(referenceDate.minusDays(90));

        DashboardRawData raw = repository.fetchDashboardRawData(
                currentDateKey,
                previous7DateKey,
                previous30DateKey,
                previous90DateKey
        );

        InstalledBinsResponse installedBins = new InstalledBinsResponse(
                raw.activeBinCount(),
                parseByTypeJson(raw.byTypeJson())
        );

        return new DashboardResponse(
                installedBins,
                buildMetric(raw.visits7dCurrent(), raw.visits7dPrevious()),
                buildMetric(raw.emptyings7dCurrent(), raw.emptyings7dPrevious()),
                buildMetric(raw.emptyingRate7dCurrent(), raw.emptyingRate7dPrevious()),
                buildMetric(raw.lowFillVisitShare90dCurrent(), raw.lowFillVisitShare90dPrevious()),
                buildMetric(raw.lowFillEmptyingShare90dCurrent(), raw.lowFillEmptyingShare90dPrevious()),
                buildMetric(raw.overfullEvents30dCurrent(), raw.overfullEvents30dPrevious())
        );
    }

    private List<BinTypeCountResponse> parseByTypeJson(String byTypeJson) {
        try {
            if (byTypeJson == null || byTypeJson.isBlank()) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(
                    byTypeJson,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse byTypeJson from dashboard query", e);
        }
    }

    private KpiMetricResponse buildMetric(BigDecimal currentValue, BigDecimal previousValue) {
        BigDecimal deltaAbsolute = currentValue.subtract(previousValue);
        BigDecimal deltaRelative = safeRelativeDelta(currentValue, previousValue);
        TrendDirection trendDirection = determineTrend(deltaAbsolute);

        return new KpiMetricResponse(
                scale(currentValue),
                scale(previousValue),
                scale(deltaAbsolute),
                scale(deltaRelative),
                trendDirection
        );
    }

    private BigDecimal safeRelativeDelta(BigDecimal currentValue, BigDecimal previousValue) {
        if (previousValue == null || previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue
                .subtract(previousValue)
                .divide(previousValue, KPI_SCALE, RoundingMode.HALF_UP);
    }

    private TrendDirection determineTrend(BigDecimal deltaAbsolute) {
        int cmp = deltaAbsolute.compareTo(BigDecimal.ZERO);
        if (cmp > 0) {
            return TrendDirection.UP;
        }
        if (cmp < 0) {
            return TrendDirection.DOWN;
        }
        return TrendDirection.FLAT;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(KPI_SCALE, RoundingMode.HALF_UP);
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