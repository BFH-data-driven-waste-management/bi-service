package ch.bfh.ddwm.biservice.bindetails.dto;

import java.math.BigDecimal;

public record DailyCountResponse(
        int dateKey,
        BigDecimal count
) {
}
