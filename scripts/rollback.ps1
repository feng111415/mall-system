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

$requiredPaths = @(
    (Join-Path $versionPath 'ruoyi-admin.jar'),
    (Join-Path $versionPath 'mall-storefront'),
    (Join-Path $versionPath 'mall-admin'),
    (Join-Path $versionPath 'database\migration-manifest.json')
)
$missing = @($requiredPaths | Where-Object { -not (Test-Path $_) })
if ($missing.Count -gt 0) {
    throw "发布包缺少回滚所需产物：$($missing -join ', ')"
}

Write-Host "Rollback target version: $Version"
Write-Host "发布包完整性校验通过。应用替换、旧 JAR 备份、健康检查和失败恢复必须由部署平台在获批后执行；本脚本不会直接覆盖运行中的生产文件。"
