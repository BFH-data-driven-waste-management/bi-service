package ch.bfh.ddwm.dssbackend.bindetails.model;

import java.math.BigDecimal;

public record BinDayFeatures(
        BigDecimal baselineAvgVisitsPerWeek90d,
        BigDecimal baselineAvgEmptyingsPerWeek90d,
        BigDecimal lowFillVisitRatio90d,
        BigDecimal overfullVisitRatio90d,
        BigDecimal notEmptiedRatio90d,
        Integer emptyingRank90d,
        BigDecimal goodWeatherSensitivityScore,
        BigDecimal badWeatherSensitivityScore,
        BigDecimal eventSensitivityScore
) {
}
