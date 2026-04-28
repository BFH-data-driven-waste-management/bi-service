package ch.bfh.ddwm.biservice.toursoverview.model;

import java.time.OffsetDateTime;

public record TourOverviewRow(
            long tourId,
            String licensePlate,
            Integer vehicleEmptyingCount,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
) {}