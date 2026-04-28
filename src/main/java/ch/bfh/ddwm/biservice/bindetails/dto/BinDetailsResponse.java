package ch.bfh.ddwm.biservice.bindetails.dto;

import java.math.BigDecimal;
import java.util.List;

public record BinDetailsResponse(
        long binId,
        String binType,
        Integer volumeLiters,
        boolean active,
        BigDecimal coordX2056,
        BigDecimal coordY2056,
        BigDecimal coordX4326,
        BigDecimal coordY4326,
        Integer lastVisitDateKey,
        Integer lastEmptyingDateKey,
        BinDayFeaturesResponse binDayFeaturesResponse,
        List<DailyCountResponse> visitFrequency90d,
        List<DailyCountResponse> emptyingFrequency90d,
        List<DailyCountResponse> fillTrend12m
) {
}
