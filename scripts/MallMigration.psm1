Set-StrictMode -Version Latest

function Get-MallMigrationCatalog {
    param(
        [Parameter(Mandatory = $true)]
        [string]$MigrationRoot
    )

    $resolvedRoot = (Resolve-Path -LiteralPath $MigrationRoot -ErrorAction Stop).Path
    $migrations = foreach ($file in Get-ChildItem -LiteralPath $resolvedRoot -File -Filter 'V*.sql') {
        if ($file.Name -match '_rollback\.sql$') {
            continue
        }
        if ($file.Name -notmatch '^V(?<version>\d+\.\d+\.\d+)__(?<description>.+)\.sql$') {
            throw "Invalid migration filename: $($file.Name)"
        }

        [pscustomobject]@{
            Version = [version]$Matches.version
            VersionText = $Matches.version
            Description = $Matches.description.Replace('_', ' ')
            Script = $file.Name
            Path = $file.FullName
            Checksum = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        }
    }

    $duplicates = @($migrations | Group-Object VersionText | Where-Object Count -gt 1)
    if ($duplicates.Count -gt 0) {
        $versions = ($duplicates | ForEach-Object Name) -join ', '
        throw "Duplicate migration versions: $versions"
    }

    return @($migrations | Sort-Object Version)
}

function ConvertTo-MallSqlLiteral {
    param(
        [AllowNull()]
        [string]$Value
    )

    if ($null -eq $Value) {
        return 'NULL'
    }
    return "'" + $Value.Replace("'", "''") + "'"
}

function Test-MallMigrationHistory {
    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Catalog,
        [Parameter(Mandatory = $true)]
        [AllowEmptyCollection()]
        [object[]]$History
    )

    $catalogByVersion = @{}
    foreach ($migration in $Catalog) {
        $catalogByVersion[$migration.VersionText] = $migration
    }

    foreach ($entry in $History) {
        if (-not $entry.Success) {
            throw "Migration $($entry.VersionText) previously failed; repair it before retrying."
        }
        if (-not $catalogByVersion.ContainsKey($entry.VersionText)) {
            throw "Applied migration $($entry.VersionText) is missing from the repository."
        }

        $migration = $catalogByVersion[$entry.VersionText]
        if ($migration.Script -ne $entry.Script) {
            throw "Migration script name changed for version $($entry.VersionText)."
        }
        if ($migration.Checksum -ne $entry.Checksum) {
            throw "Migration checksum changed for version $($entry.VersionText): $($migration.Script)"
        }
    }

    $applied = @{}
    foreach ($entry in $History) {
        $applied[$entry.VersionText] = $true
    }
    $pending = @($Catalog | Where-Object { -not $applied.ContainsKey($_.VersionText) })

    if ($History.Count -gt 0 -and $pending.Count -gt 0) {
        $latestApplied = ($History | ForEach-Object { [version]$_.VersionText } | Sort-Object | Select-Object -Last 1)
        $outOfOrder = @($pending | Where-Object { $_.Version -lt $latestApplied })
        if ($outOfOrder.Count -gt 0) {
            throw "Out-of-order migrations detected below applied version $latestApplied."
        }
    }

    return $pending
}

Export-ModuleMember -Function Get-MallMigrationCatalog, ConvertTo-MallSqlLiteral, Test-MallMigrationHistory
