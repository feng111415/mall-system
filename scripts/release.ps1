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
Push-Location $projectRoot
try {
    cmd /c "mvn.cmd -pl ruoyi-admin -am -DskipTests package"
    if ($LASTEXITCODE -ne 0) {
        throw "后端 JAR 构建失败"
    }
} finally {
    Pop-Location
}
& (Join-Path $PSScriptRoot 'build-mall-storefront.ps1')
& (Join-Path $PSScriptRoot 'build-mall-admin.ps1')
if (-not (Test-Path (Join-Path $projectRoot 'ruoyi-admin\target\ruoyi-admin.jar'))) {
    throw "未找到后端 JAR：ruoyi-admin\target\ruoyi-admin.jar"
}
Copy-Item (Join-Path $projectRoot 'ruoyi-admin\target\ruoyi-admin.jar') (Join-Path $outputPath 'ruoyi-admin.jar')
Copy-Item (Join-Path $projectRoot 'mall-storefront\dist') (Join-Path $outputPath 'mall-storefront') -Recurse
Copy-Item (Join-Path $projectRoot 'mall-admin\dist') (Join-Path $outputPath 'mall-admin') -Recurse

$databasePath = Join-Path $outputPath 'database'
$migrationPath = Join-Path $databasePath 'migrations'
New-Item -ItemType Directory -Force -Path $migrationPath | Out-Null
Import-Module (Join-Path $PSScriptRoot 'MallMigration.psm1') -Force
$migrationCatalog = @(Get-MallMigrationCatalog -MigrationRoot (Join-Path $projectRoot 'sql'))
foreach ($migration in $migrationCatalog) {
    Copy-Item -LiteralPath $migration.Path -Destination $migrationPath
}
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'MallMigration.psm1') -Destination $databasePath
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'invoke-db-migrations.ps1') -Destination $databasePath
$migrationCatalog |
    Select-Object VersionText, Script, Checksum |
    ConvertTo-Json |
    Set-Content -LiteralPath (Join-Path $databasePath 'migration-manifest.json') -Encoding UTF8

git -C $projectRoot tag $Version
Write-Host "已生成发布包：$outputPath"
