package ch.bfh.ddwm.biservice.dashboard.dto;

import java.util.List;

public record InstalledBinsResponse(
        long total,
        List<CountOfBinTypeResponse> countOfBinType
) {}
