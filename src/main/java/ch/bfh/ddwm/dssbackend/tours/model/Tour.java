package ch.bfh.ddwm.dssbackend.tours.model;

import java.time.OffsetDateTime;
import java.util.List;

public record Tour(
        long id,
        String licensePlate,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        List<BinVisit> binVisits
) {}
