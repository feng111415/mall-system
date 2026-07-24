param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^v\d+\.\d+\.\d+$')]
    [string]$Version,
    [string]$ReleaseRoot = 'release'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$versionPath = Join-Path $projectRoot (Join-Path $ReleaseRoot $Version)

if (-not (Test-Path $versionPath)) {
    throw "Release package not found: $versionPath"
}

Write-Host "Rollback target version: $Version"
Write-Host "The script only validates local packages. Production replacement requires deployment approval."
