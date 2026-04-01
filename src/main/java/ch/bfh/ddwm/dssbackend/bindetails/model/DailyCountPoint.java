package ch.bfh.ddwm.dssbackend.bindetails.model;

import java.math.BigDecimal;

public record DailyCountPoint(
        int dateKey,
        BigDecimal count
) {
}
