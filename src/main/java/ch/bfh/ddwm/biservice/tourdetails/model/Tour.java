package ch.bfh.ddwm.biservice.tourdetails.model;

import ch.bfh.ddwm.biservice.common.model.BinVisit;
import ch.bfh.ddwm.biservice.common.model.VehicleEmptying;

import java.time.OffsetDateTime;
import java.util.List;

public record Tour(
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
