package ch.bfh.ddwm.dssbackend.dashboard.dto;

import java.util.List;

public record InstalledBinsResponse(
        long total,
        List<CountOfBinTypeResponse> countOfBinType
) {}
