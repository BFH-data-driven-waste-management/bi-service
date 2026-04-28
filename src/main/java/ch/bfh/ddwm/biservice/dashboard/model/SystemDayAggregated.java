package ch.bfh.ddwm.biservice.dashboard.model;

import java.math.BigDecimal;

public record SystemDayAggregated(
        int activeBinCount,
        int visits7dCurrent,
        int visits7dPrevious,
        int emptyings7dCurrent,
        int emptyings7dPrevious,
        BigDecimal emptyingRate7dCurrent,
        BigDecimal emptyingRate7dPrevious,
        BigDecimal lowFillVisitShare90dCurrent,
        BigDecimal lowFillVisitShare90dPrevious,
        BigDecimal lowFillEmptyingShare90dCurrent,
        BigDecimal lowFillEmptyingShare90dPrevious,
        int overfullEvents30dCurrent,
        int overfullEvents30dPrevious
) { }
