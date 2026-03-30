package ch.bfh.ddwm.dssbackend.dashboard.dto;

import java.math.BigDecimal;

public record DashboardRawData(
        long activeBinCount,
        BigDecimal visits7dCurrent,
        BigDecimal visits7dPrevious,
        BigDecimal emptyings7dCurrent,
        BigDecimal emptyings7dPrevious,
        BigDecimal emptyingRate7dCurrent,
        BigDecimal emptyingRate7dPrevious,
        BigDecimal lowFillVisitShare90dCurrent,
        BigDecimal lowFillVisitShare90dPrevious,
        BigDecimal lowFillEmptyingShare90dCurrent,
        BigDecimal lowFillEmptyingShare90dPrevious,
        BigDecimal overfullEvents30dCurrent,
        BigDecimal overfullEvents30dPrevious
) { }
