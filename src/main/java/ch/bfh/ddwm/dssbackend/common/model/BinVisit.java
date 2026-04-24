package ch.bfh.ddwm.dssbackend.common.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BinVisit(
        long id,
        long binId,
        int sequenceInTour,
        OffsetDateTime eventTimestamp,
        String visitAction,
        String fillLevel,
        BigDecimal binCoordX,
        BigDecimal binCoordY,
        String binType
) {}
