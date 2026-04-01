package ch.bfh.ddwm.dssbackend.binlist.model;

import java.math.BigDecimal;

public record BinListItem(
        long binKey,
        String type,
        BigDecimal avgWeeklyVisits90d,
        BigDecimal lowFillVisitRatio90d,
        BigDecimal overfullVisitRatio90d
) {
}
