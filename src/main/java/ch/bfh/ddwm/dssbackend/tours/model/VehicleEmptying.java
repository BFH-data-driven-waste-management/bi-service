package ch.bfh.ddwm.dssbackend.tours.model;

import java.time.OffsetDateTime;

public record VehicleEmptying(
        long id,
        int sequenceInTour,
        OffsetDateTime eventTimestamp
) {}
