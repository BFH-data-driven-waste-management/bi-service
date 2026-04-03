package ch.bfh.ddwm.dssbackend.bindetails.model;

import java.math.BigDecimal;

public record BinDetails(
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
        BinDayFeatures featureSnapshot
) {
}
