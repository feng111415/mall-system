$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendRoot = Join-Path $projectRoot 'ruoyi-ui'

if (-not (Test-Path (Join-Path $frontendRoot 'package.json'))) {
    throw 'ruoyi-ui/package.json was not found'
}

Push-Location $frontendRoot
try {
    cmd /c npm run build:prod
    if ($LASTEXITCODE -ne 0) { throw 'RuoYi admin frontend build failed' }
} finally {
    Pop-Location
}
