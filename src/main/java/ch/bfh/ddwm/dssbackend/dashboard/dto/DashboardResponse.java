package ch.bfh.ddwm.dssbackend.dashboard.dto;

public record DashboardResponse(
    InstalledBinsResponse installedBins,
    KpiMetricResponse visits7d,
    KpiMetricResponse emptyings7d,
    KpiMetricResponse emptyingRate7d,
    KpiMetricResponse lowFillVisitShare90d,
    KpiMetricResponse lowFillEmptyingShare90d,
    KpiMetricResponse overfullEvents30d
) {}
