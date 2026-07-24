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
    throw "未找到版本发布包：$versionPath"
}

Write-Host "回滚目标版本：$Version"
Write-Host "当前脚本只完成本地发布目录校验，生产替换动作需要部署环境确认后执行。"
