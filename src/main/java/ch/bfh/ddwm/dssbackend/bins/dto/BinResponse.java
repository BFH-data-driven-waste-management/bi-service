package ch.bfh.ddwm.dssbackend.bins.dto;

import java.math.BigDecimal;

public record BinResponse(
        String type,
        boolean isActive,
        BigDecimal coordX2056,
        BigDecimal coordY2056,
        BigDecimal coordX4326,
        BigDecimal coordY4326
) { }
