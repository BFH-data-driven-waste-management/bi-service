package ch.bfh.ddwm.dssbackend.dashboard.dto;

public record CountOfBinTypeResponse(
        String type,
        long count
) {}
