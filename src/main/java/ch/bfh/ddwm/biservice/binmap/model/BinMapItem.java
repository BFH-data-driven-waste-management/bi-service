package ch.bfh.ddwm.biservice.binmap.model;

import java.math.BigDecimal;

public record BinMapItem(
        long binId,
        String type,
        boolean isActive,
        BigDecimal coordX4326,
        BigDecimal coordY4326,
        BigDecimal coordX2056,
        BigDecimal coordY2056,
        Integer daysSinceLastEmptying
) {
}
