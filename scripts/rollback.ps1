param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^v\d+\.\d+\.\d+$')]
    [string]$Version,
    [string]$ReleaseRoot = 'release'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$versionPath = Join-Path $projectRoot (Join-Path $ReleaseRoot $Version)

if (-not (Test-Path -LiteralPath $versionPath)) {
    throw "Release package not found: $versionPath"
}

$requiredPaths = @(
    (Join-Path $versionPath 'ruoyi-admin.jar'),
    (Join-Path $versionPath 'mall-storefront'),
    (Join-Path $versionPath 'ruoyi-ui'),
    (Join-Path $versionPath 'database\migration-manifest.json')
)
$missing = @($requiredPaths | Where-Object { -not (Test-Path -LiteralPath $_) })
if ($missing.Count -gt 0) {
    throw "Release package is incomplete: $($missing -join ', ')"
}

Write-Host "Rollback target version: $Version"
Write-Host 'Release package validation passed.'
Write-Host 'The deployment platform must back up, replace, health-check, and restore artifacts after approval.'
