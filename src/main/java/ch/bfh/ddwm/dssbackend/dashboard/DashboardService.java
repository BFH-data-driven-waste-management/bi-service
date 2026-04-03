package ch.bfh.ddwm.dssbackend.dashboard;

import ch.bfh.ddwm.dssbackend.dashboard.dto.CountOfBinTypeResponse;
import ch.bfh.ddwm.dssbackend.dashboard.dto.DashboardResponse;
import ch.bfh.ddwm.dssbackend.dashboard.dto.InstalledBinsResponse;
import ch.bfh.ddwm.dssbackend.dashboard.dto.KpiMetricResponse;
import ch.bfh.ddwm.dssbackend.dashboard.model.SystemDayAggregated;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static ch.bfh.ddwm.dssbackend.common.DateKeyHelper.fromDateKey;
import static ch.bfh.ddwm.dssbackend.common.DateKeyHelper.toDateKey;

@Service
public class DashboardService {

    private static final int KPI_SCALE = 4;

    private final DashboardRepository repository;

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    public DashboardResponse getDashboard() {
        int todayDateKey = toDateKey(LocalDate.now());

        if (repository.hasSystemSummaryForDate(todayDateKey)) {
            throw new IllegalStateException("No fact_system_day snapshot available up to today");
        }

        LocalDate referenceDate = fromDateKey(todayDateKey);

        SystemDayAggregated systemDayAggregated = repository.systemDaySummaryAggregatedByDate(
                        todayDateKey,
                        toDateKey(referenceDate.minusDays(7)),
                        toDateKey(referenceDate.minusDays(30)),
                        toDateKey(referenceDate.minusDays(90))
                )
                .orElseThrow(() -> new IllegalStateException("No fact_system_day snapshot available for today"));

        InstalledBinsResponse installedBins = new InstalledBinsResponse(
                systemDayAggregated.activeBinCount(),
                mapCountOfBinTypes()
        );

        KpiMetricResponse visits = buildIntegerMetric(systemDayAggregated.visits7dCurrent(), systemDayAggregated.visits7dPrevious());
        KpiMetricResponse emptyings = buildIntegerMetric(systemDayAggregated.emptyings7dCurrent(), systemDayAggregated.emptyings7dPrevious());
        KpiMetricResponse emptyingRate = buildDecimalMetric(systemDayAggregated.emptyingRate7dCurrent(), systemDayAggregated.emptyingRate7dPrevious());
        KpiMetricResponse lowFillVisitShare = buildDecimalMetric(systemDayAggregated.lowFillVisitShare90dCurrent(), systemDayAggregated.lowFillVisitShare90dPrevious());
        KpiMetricResponse lowFillEmptyingShare = buildDecimalMetric(systemDayAggregated.lowFillEmptyingShare90dCurrent(), systemDayAggregated.lowFillEmptyingShare90dPrevious());
        KpiMetricResponse overfullEvents = buildIntegerMetric(systemDayAggregated.overfullEvents30dCurrent(), systemDayAggregated.overfullEvents30dPrevious());

        return new DashboardResponse(
                installedBins,
                visits,
                emptyings,
                emptyingRate,
                lowFillVisitShare,
                lowFillEmptyingShare,
                overfullEvents
        );
    }

    private List<CountOfBinTypeResponse> mapCountOfBinTypes() {
        var todayDateKey = toDateKey(LocalDate.now());
        return repository.findActiveBinCountByTypeFilterByDate(todayDateKey).stream()
                .map(countOfBinType -> new CountOfBinTypeResponse(countOfBinType.type(), countOfBinType.count()))
                .toList();
    }

    private KpiMetricResponse buildIntegerMetric(int currentValue, int previousValue) {
        return buildDecimalMetric(BigDecimal.valueOf(currentValue), BigDecimal.valueOf(previousValue));
    }

    private KpiMetricResponse buildDecimalMetric(BigDecimal currentValue, BigDecimal previousValue) {
        BigDecimal relativeDelta = calculateRelativeChangeOrZero(currentValue, previousValue);

        return new KpiMetricResponse(
                scale(currentValue),
                scale(relativeDelta)
        );
    }

    private BigDecimal calculateRelativeChangeOrZero(BigDecimal currentValue, BigDecimal previousValue) {
        if (previousValue.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return currentValue
                .subtract(previousValue)
                .divide(previousValue, KPI_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(KPI_SCALE, RoundingMode.HALF_UP);
    }
}