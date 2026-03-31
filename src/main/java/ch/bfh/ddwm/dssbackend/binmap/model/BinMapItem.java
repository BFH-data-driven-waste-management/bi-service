package ch.bfh.ddwm.dssbackend.binmap.model;

import java.math.BigDecimal;

public record BinMapItem(
        long binKey,
        String type,
        boolean isActive,
        BigDecimal coordX4326,
        BigDecimal coordY4326
) {
}
