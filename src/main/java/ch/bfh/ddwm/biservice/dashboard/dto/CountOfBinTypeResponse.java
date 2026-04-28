package ch.bfh.ddwm.biservice.dashboard.dto;

public record CountOfBinTypeResponse(
        String type,
        long count
) {}
