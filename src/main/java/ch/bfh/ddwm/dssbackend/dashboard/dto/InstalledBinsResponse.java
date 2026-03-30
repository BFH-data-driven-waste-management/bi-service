package ch.bfh.ddwm.dssbackend.dashboard.dto;

import ch.bfh.ddwm.dssbackend.dashboard.model.CountOfBinType;

import java.util.List;

public record InstalledBinsResponse(
        long total,
        List<CountOfBinType> countOfBinType
) {}
