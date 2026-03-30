package ch.bfh.ddwm.dssbackend.dashboard;

import ch.bfh.ddwm.dssbackend.dashboard.dto.*;
import ch.bfh.ddwm.dssbackend.dashboard.model.SystemDayAggregated;
import ch.bfh.ddwm.dssbackend.dashboard.dto.InstalledBinsResponse;
import ch.bfh.ddwm.dssbackend.dashboard.dto.KpiMetricResponse;
import ch.bfh.ddwm.dssbackend.dashboard.dto.TrendDirectionResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class DashboardService {

    private static final int KPI_SCALE = 4;

    private final DashboardRepository repository;

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

        Optional<SystemDayAggregated> systemDayAggregated = repository.fetchDashboardRawData(
                currentDateKey,
                previous7DateKey,
                previous30DateKey,
                previous90DateKey
        );

        if (systemDayAggregated.isEmpty()) {
            throw new IllegalStateException("No fact_system_day snapshot available for today");
        }

        InstalledBinsResponse installedBins = new InstalledBinsResponse(
                systemDayAggregated.get().activeBinCount(),
                repository.findActiveBinCountByType()
        );

        return new DashboardResponse(
                installedBins,
                buildMetric(systemDayAggregated.get().visits7dCurrent(), systemDayAggregated.get().visits7dPrevious()),
                buildMetric(systemDayAggregated.get().emptyings7dCurrent(), systemDayAggregated.get().emptyings7dPrevious()),
                buildMetric(systemDayAggregated.get().emptyingRate7dCurrent(), systemDayAggregated.get().emptyingRate7dPrevious()),
                buildMetric(systemDayAggregated.get().lowFillVisitShare90dCurrent(), systemDayAggregated.get().lowFillVisitShare90dPrevious()),
                buildMetric(systemDayAggregated.get().lowFillEmptyingShare90dCurrent(), systemDayAggregated.get().lowFillEmptyingShare90dPrevious()),
                buildMetric(systemDayAggregated.get().overfullEvents30dCurrent(), systemDayAggregated.get().overfullEvents30dPrevious())
        );
    }

    private KpiMetricResponse buildMetric(BigDecimal currentValue, BigDecimal previousValue) {
        BigDecimal deltaAbsolute = currentValue.subtract(previousValue);
        BigDecimal deltaRelative = safeRelativeDelta(currentValue, previousValue);
        TrendDirectionResponse trendDirection = determineTrend(deltaAbsolute);

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

    private TrendDirectionResponse determineTrend(BigDecimal deltaAbsolute) {
        int cmp = deltaAbsolute.compareTo(BigDecimal.ZERO);
        if (cmp > 0) {
            return TrendDirectionResponse.UP;
        }
        if (cmp < 0) {
            return TrendDirectionResponse.DOWN;
        }
        return TrendDirectionResponse.FLAT;
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
