package ch.bfh.ddwm.biservice.bindetails.model;

import java.time.OffsetDateTime;

public record BinVisitHistory(
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
