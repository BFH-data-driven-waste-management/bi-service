package ch.bfh.ddwm.dssbackend.dashboard.dto;

import java.math.BigDecimal;

public record KpiMetricResponse(
        BigDecimal value,
        BigDecimal previousValue,
        BigDecimal deltaAbsolute,
        BigDecimal deltaRelative,
        TrendDirectionResponse trendDirectionResponse
) {}
