package ch.bfh.ddwm.dssbackend.tourdetails.model;

import java.time.OffsetDateTime;

public record TourRow(
            long tourId,
            String licensePlate,
            Integer visitCount,
            Integer emptiedVisitCount,
            Integer notEmptiedVisitCount,
            Integer lowFillVisitCount,
            Integer highFillVisitCount,
            Integer overfullVisitCount,
            Integer vehicleEmptyingCount,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt
) {}