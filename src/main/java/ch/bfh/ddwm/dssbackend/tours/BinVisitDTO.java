package ch.bfh.ddwm.dssbackend.tours;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BinVisitDTO(
        long id,
        int sequenceInTour,
        OffsetDateTime eventTimestamp,
        String visitAction,
        String fillLevel,
        BigDecimal binCoordX,
        BigDecimal binCoordY,
        String binType
) {}
