package ch.bfh.ddwm.dssbackend.tours.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record TourDTO(
        long id,
        String licensePlate,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        List<BinVisitDTO> binVisits
) {}
