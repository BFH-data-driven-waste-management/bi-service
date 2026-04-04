package ch.bfh.ddwm.dssbackend.tours.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record TourDTO(
        long id,
        String licensePlate,
        Integer vehicleEmptyingCount,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        List<VehicleEmptyingDTO> vehicleEmptyings,
        List<BinVisitDTO> binVisits
) {}
