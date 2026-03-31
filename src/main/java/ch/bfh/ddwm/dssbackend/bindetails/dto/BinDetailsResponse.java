package ch.bfh.ddwm.dssbackend.bindetails.dto;

import java.math.BigDecimal;

public record BinDetailsResponse(
        long binKey,
        String type,
        Integer volumeLiters,
        long zoneKey,
        boolean isActive,
        BigDecimal coordX2056,
        BigDecimal coordY2056,
        BigDecimal coordX4326,
        BigDecimal coordY4326,
        BinFeatureSnapshotResponse features
) {
}
