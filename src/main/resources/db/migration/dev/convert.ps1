# Flyway rename helper
    [CmdletBinding(SupportsShouldProcess = $true)]
    param(
        [Parameter(Mandatory = $false)]
        [string]$Folder = "."
    )

    Set-StrictMode -Version Latest
    $ErrorActionPreference = "Stop"

    $Folder = (Resolve-Path -LiteralPath $Folder).Path

    # Mapping: raw file -> Flyway file name
    $RenameMap = [ordered]@{
        "dim_fill_level.sql"         = "V1000__dim_fill_level.sql"
        "dim_action.sql"             = "V1001__dim_action.sql"
        "dim_connectivity_state.sql" = "V1002__dim_connectivity_state.sql"
        "dim_date.sql"               = "V1003__dim_date.sql"
        "dim_zone.sql"               = "V1004__dim_zone.sql"
        "dim_vehicle.sql"            = "V1005__dim_vehicle.sql"
        "dim_bin.sql"                = "V1006__dim_bin.sql"
        "dim_event.sql"              = "V1007__dim_event.sql"
        "fact_tour.sql"              = "V1008__fact_tour.sql"
        "fact_bin_visit.sql"         = "V1009__fact_bin_visit.sql"
        "fact_vehicle_emptying.sql"  = "V1010__fact_vehicle_emptying.sql"
        "fact_bin_status_change.sql" = "V1011__fact_bin_status_change.sql"
        "fact_weather_day.sql"       = "V1012__fact_weather_day.sql"
        "fact_event_zone_day.sql"    = "V1013__fact_event_zone_day.sql"
        "fact_bin_daily_snapshot.sql"= "V1014__fact_bin_day_snapshot.sql"
        "system_day_summary.sql"     = "V1015__derived_system_day.sql"
        "bin_day_features.sql"       = "V1016__derived_bin_day_features.sql"
    }

    Write-Host "Working folder: $Folder"
    Write-Host ""

    # 1) Delete existing Flyway files
    foreach ($targetName in $RenameMap.Values) {
        $targetPath = Join-Path -Path $Folder -ChildPath $targetName

        if (Test-Path -LiteralPath $targetPath) {
            if ($PSCmdlet.ShouldProcess($targetPath, "Delete existing Flyway file")) {
                Remove-Item -LiteralPath $targetPath -Force
                Write-Host "Deleted: $targetName"
            }
        }
    }

    Write-Host ""

    # 2) Rename raw files to Flyway names
    foreach ($entry in $RenameMap.GetEnumerator()) {
        $sourceName = $entry.Key
        $targetName = $entry.Value

        $sourcePath = Join-Path -Path $Folder -ChildPath $sourceName
        $targetPath = Join-Path -Path $Folder -ChildPath $targetName

        if (-not (Test-Path -LiteralPath $sourcePath)) {
            Write-Warning "Source file missing: $sourceName"
            continue
        }

        if (Test-Path -LiteralPath $targetPath) {
            throw "Target file still exists after delete pass: $targetName"
        }

        if ($PSCmdlet.ShouldProcess($sourcePath, "Rename to $targetName")) {
            Rename-Item -LiteralPath $sourcePath -NewName $targetName
            Write-Host "Renamed: $sourceName -> $targetName"
        }
    }

    Write-Host ""
    Write-Host "Done."