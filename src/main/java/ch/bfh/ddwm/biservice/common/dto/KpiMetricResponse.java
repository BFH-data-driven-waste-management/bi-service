package ch.bfh.ddwm.biservice.common.dto;

import java.math.BigDecimal;

public record KpiMetricResponse(
        BigDecimal value,
        BigDecimal deltaRelative
) {
}
