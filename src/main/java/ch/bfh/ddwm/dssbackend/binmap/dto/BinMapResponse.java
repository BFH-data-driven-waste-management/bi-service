package ch.bfh.ddwm.dssbackend.binmap.dto;

import java.math.BigDecimal;

public record BinMapResponse(
        long binKey,
        String type,
        boolean isActive,
        BigDecimal coordX4326,
        BigDecimal coordY4326
) {
}
