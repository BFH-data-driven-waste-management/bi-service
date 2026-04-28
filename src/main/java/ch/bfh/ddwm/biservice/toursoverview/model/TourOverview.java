package ch.bfh.ddwm.biservice.toursoverview.model;

import ch.bfh.ddwm.biservice.common.model.BinVisit;
import ch.bfh.ddwm.biservice.common.model.VehicleEmptying;

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
