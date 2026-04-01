package ch.bfh.ddwm.dssbackend.bindetails.dto;

import java.math.BigDecimal;

public record DailyCountResponse(
        int dateKey,
        BigDecimal count
) {
}
