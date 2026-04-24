package ch.bfh.ddwm.dssbackend.tourdetails.model;

import ch.bfh.ddwm.dssbackend.common.model.BinVisit;
import ch.bfh.ddwm.dssbackend.common.model.VehicleEmptying;

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
