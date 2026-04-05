package ch.bfh.ddwm.dssbackend.common;

import ch.bfh.ddwm.dssbackend.common.dto.KpiMetricResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class KpiMetricCalculator {

    private static final int KPI_SCALE = 4;

    private KpiMetricCalculator() {
    }

    public static KpiMetricResponse buildIntegerMetric(int currentValue, int previousValue) {
        return buildDecimalMetric(BigDecimal.valueOf(currentValue), BigDecimal.valueOf(previousValue));
    }

    public static KpiMetricResponse buildDecimalMetric(BigDecimal currentValue, BigDecimal previousValue) {
        BigDecimal normalizedCurrentValue = currentValue == null ? BigDecimal.ZERO : currentValue;
        BigDecimal normalizedPreviousValue = previousValue == null ? BigDecimal.ZERO : previousValue;
        BigDecimal relativeDelta = calculateRelativeChangeOrZero(normalizedCurrentValue, normalizedPreviousValue);

        return new KpiMetricResponse(
                scale(normalizedCurrentValue),
                scale(relativeDelta)
        );
    }

    private static BigDecimal calculateRelativeChangeOrZero(BigDecimal currentValue, BigDecimal previousValue) {
        if (previousValue.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return currentValue
                .subtract(previousValue)
                .divide(previousValue, KPI_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(KPI_SCALE, RoundingMode.HALF_UP);
    }
}
