package ch.bfh.ddwm.dssbackend.bindetails.dto;

import java.time.OffsetDateTime;

public record BinVisitHistoryResponse(
        Long binVisitId,
        Long binId,
        Long tourId,
        Integer sequenceInTour,
        OffsetDateTime eventTimestamp,
        String fillLevelCode,
        String actionCode,
        String licensePlate
) {
}
