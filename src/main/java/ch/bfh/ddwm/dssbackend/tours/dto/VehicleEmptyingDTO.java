package ch.bfh.ddwm.dssbackend.tours.dto;

import java.time.OffsetDateTime;

public record VehicleEmptyingDTO(
        long id,
        int sequenceInTour,
        OffsetDateTime eventTimestamp
) {}
