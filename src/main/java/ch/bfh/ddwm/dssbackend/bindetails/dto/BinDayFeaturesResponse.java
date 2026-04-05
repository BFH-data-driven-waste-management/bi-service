package ch.bfh.ddwm.dssbackend.bindetails.dto;

import ch.bfh.ddwm.dssbackend.common.dto.KpiMetricResponse;

import java.math.BigDecimal;

public record BinDayFeaturesResponse(
        KpiMetricResponse baselineAvgVisitsPerWeek90d,
        KpiMetricResponse baselineAvgEmptyingsPerWeek90d,
        BigDecimal lowFillVisitRatio90d,
        BigDecimal notEmptiedRatio90d,
        Integer emptyingRank90d,
        BigDecimal weatherSensitivityScore,
        BigDecimal rainSensitivityScore,
        BigDecimal sunSensitivityScore,
        BigDecimal heatSensitivityScore,
        BigDecimal eventSensitivityScore
) {
}
