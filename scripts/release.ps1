param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^v\d+\.\d+\.\d+$')]
    [string]$Version,
    [string]$OutputRoot = 'release'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$outputPath = Join-Path $projectRoot (Join-Path $OutputRoot $Version)

foreach ($debugVariable in @('DEBUG', 'TRACE')) {
    if (-not [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($debugVariable, 'Process'))) {
        throw "$debugVariable must be unset before creating a release package"
    }
}

if (Test-Path $outputPath) {
    throw "发布目录已存在：$outputPath"
}

New-Item -ItemType Directory -Force -Path $outputPath | Out-Null
Push-Location $projectRoot
try {
    cmd /c "mvn.cmd -pl ruoyi-admin -am clean package -DskipTests"
    if ($LASTEXITCODE -ne 0) {
        throw "后端 JAR 构建失败"
    }
    $compilerErrorMarker = 'Unresolved compilation problems'
    $invalidClasses = @(
        Get-ChildItem -Path $projectRoot -Recurse -Filter '*.class' |
            Where-Object { $_.FullName -match '[\\/]target[\\/]classes[\\/]' } |
            Where-Object {
                [Text.Encoding]::UTF8.GetString([IO.File]::ReadAllBytes($_.FullName)).Contains($compilerErrorMarker)
            }
    )
    if ($invalidClasses.Count -gt 0) {
        $relativePaths = @($invalidClasses | ForEach-Object { $_.FullName.Substring($projectRoot.Length + 1) })
        throw "Backend build contains unresolved compiler-error bytecode: $($relativePaths -join ', ')"
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
Copy-Item (Join-Path $projectRoot 'ruoyi-ui\dist') (Join-Path $outputPath 'ruoyi-ui') -Recurse

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

Write-Host "已生成发布包：$outputPath"
