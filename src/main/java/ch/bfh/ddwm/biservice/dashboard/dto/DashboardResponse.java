package ch.bfh.ddwm.biservice.dashboard.dto;

import ch.bfh.ddwm.biservice.common.dto.KpiMetricResponse;

public record DashboardResponse(
    InstalledBinsResponse installedBins,
    KpiMetricResponse visits7d,
    KpiMetricResponse emptyings7d,
    KpiMetricResponse emptyingRate7d,
    KpiMetricResponse lowFillVisitShare90d,
    KpiMetricResponse lowFillEmptyingShare90d,
    KpiMetricResponse overfullEvents30d
) {}
