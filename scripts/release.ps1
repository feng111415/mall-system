param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^v\d+\.\d+\.\d+$')]
    [string]$Version,
    [string]$OutputRoot = 'release'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$outputPath = Join-Path $projectRoot (Join-Path $OutputRoot $Version)

if (Test-Path $outputPath) {
    throw "发布目录已存在：$outputPath"
}

New-Item -ItemType Directory -Force -Path $outputPath | Out-Null
& (Join-Path $PSScriptRoot 'build-mall-storefront.ps1')
& (Join-Path $PSScriptRoot 'build-mall-admin.ps1')
Copy-Item (Join-Path $projectRoot 'mall-storefront\dist') (Join-Path $outputPath 'mall-storefront') -Recurse
Copy-Item (Join-Path $projectRoot 'mall-admin\dist') (Join-Path $outputPath 'mall-admin') -Recurse
git -C $projectRoot tag $Version
Write-Host "已生成发布包：$outputPath"
