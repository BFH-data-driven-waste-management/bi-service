package ch.bfh.ddwm.biservice.common.model;

import java.time.OffsetDateTime;

public record VehicleEmptying(
        long id,
        int sequenceInTour,
        OffsetDateTime eventTimestamp
) {}
