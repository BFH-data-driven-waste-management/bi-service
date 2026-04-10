package ch.bfh.ddwm.dssbackend.tours.model;

import java.time.OffsetDateTime;

public record TourOverviewRow(
            long tourId,
            String licensePlate,
            Integer vehicleEmptyingCount,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
) {}