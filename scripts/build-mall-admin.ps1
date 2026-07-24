param(
    [ValidateSet('development', 'production')]
    [string]$Mode = 'production'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $projectRoot 'mall-admin'

if (-not (Test-Path (Join-Path $frontendRoot 'package.json'))) {
    throw "未找到 mall-admin/package.json"
}

Push-Location $frontendRoot
try {
    if ($Mode -eq 'development') {
        cmd /c npm run build -- --mode development
    } else {
        cmd /c npm run build
    }
} finally {
    Pop-Location
}
