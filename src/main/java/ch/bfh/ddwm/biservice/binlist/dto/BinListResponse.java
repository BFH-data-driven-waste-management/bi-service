package ch.bfh.ddwm.biservice.binlist.dto;

import java.math.BigDecimal;

public record BinListResponse(
        long binId,
        String type,
        boolean isActive,
        BigDecimal avgWeeklyVisits90d,
        BigDecimal lowFillVisitRatio90d,
        BigDecimal overfullVisitRatio90d,
        BigDecimal coordX2056,
        BigDecimal coordY2056
) {
}
