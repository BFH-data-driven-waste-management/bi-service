package ch.bfh.ddwm.dssbackend.tourdetails.dto;

import ch.bfh.ddwm.dssbackend.common.dto.BinVisit;
import ch.bfh.ddwm.dssbackend.common.dto.VehicleEmptying;

import java.time.OffsetDateTime;
import java.util.List;

public record TourResponse(
        long id,
        String licensePlate,
        Integer visitCount,
        Integer emptiedVisitCount,
        Integer notEmptiedVisitCount,
        Integer lowFillVisitCount,
        Integer highFillVisitCount,
        Integer overfullVisitCount,
        Integer vehicleEmptyingCount,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        List<VehicleEmptying> vehicleEmptyings,
        List<BinVisit> binVisits
) {}
