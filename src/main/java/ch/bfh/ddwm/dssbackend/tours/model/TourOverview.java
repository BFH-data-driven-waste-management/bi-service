package ch.bfh.ddwm.dssbackend.tours.model;

import java.time.OffsetDateTime;
import java.util.List;

public record TourOverview(
        long id,
        String licensePlate,
        Integer vehicleEmptyingCount,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        List<VehicleEmptying> vehicleEmptyings,
        List<BinVisit> binVisits
) {}
