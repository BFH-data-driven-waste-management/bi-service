package ch.bfh.ddwm.biservice.common.dto;

import java.time.OffsetDateTime;

public record VehicleEmptying(
        long id,
        int sequenceInTour,
        OffsetDateTime eventTimestamp
) {}
