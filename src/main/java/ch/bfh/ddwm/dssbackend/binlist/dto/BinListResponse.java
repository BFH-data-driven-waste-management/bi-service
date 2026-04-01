package ch.bfh.ddwm.dssbackend.binlist.dto;

import java.math.BigDecimal;

public record BinListResponse(
        long binKey,
        String type,
        BigDecimal avgWeeklyVisits90d,
        BigDecimal lowFillVisitRatio90d,
        BigDecimal overfullVisitRatio90d
) {
}
