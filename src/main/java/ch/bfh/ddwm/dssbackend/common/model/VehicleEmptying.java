package ch.bfh.ddwm.dssbackend.common.model;

import java.time.OffsetDateTime;

public record VehicleEmptying(
        long id,
        int sequenceInTour,
        OffsetDateTime eventTimestamp
) {}
