package ch.bfh.ddwm.biservice.bindetails.model;

import java.math.BigDecimal;

public record DailyCountPoint(
        int dateKey,
        BigDecimal count
) {
}
