package ch.bfh.ddwm.dssbackend.toursoverview.dto;

import ch.bfh.ddwm.dssbackend.common.dto.BinVisit;
import ch.bfh.ddwm.dssbackend.common.dto.VehicleEmptying;

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
