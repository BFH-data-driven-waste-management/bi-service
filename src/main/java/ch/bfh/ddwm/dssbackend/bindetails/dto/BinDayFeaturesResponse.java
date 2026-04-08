package ch.bfh.ddwm.dssbackend.bindetails.dto;

import ch.bfh.ddwm.dssbackend.common.dto.KpiMetricResponse;

import java.math.BigDecimal;

public record BinDayFeaturesResponse(
        KpiMetricResponse baselineAvgVisitsPerWeek90d,
        KpiMetricResponse baselineAvgEmptyingsPerWeek90d,
        BigDecimal lowFillVisitRatio90d,
        BigDecimal overfullVisitRatio90d,
        BigDecimal notEmptiedRatio90d,
        Integer emptyingRank90d,
        BigDecimal goodWeatherSensitivityScore,
        BigDecimal badWeatherSensitivityScore,
        BigDecimal eventSensitivityScore
) {
}
