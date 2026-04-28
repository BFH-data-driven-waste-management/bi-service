package ch.bfh.ddwm.biservice.toursoverview.dto;

import ch.bfh.ddwm.biservice.common.dto.BinVisit;
import ch.bfh.ddwm.biservice.common.dto.VehicleEmptying;

import java.time.OffsetDateTime;
import java.util.List;

public record TourOverviewResponse(
        long id,
        String licensePlate,
        Integer vehicleEmptyingCount,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        List<VehicleEmptying> vehicleEmptyings,
        List<BinVisit> binVisits
) {}
