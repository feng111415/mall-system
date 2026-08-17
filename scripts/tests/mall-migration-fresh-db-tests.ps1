param(
    [string]$MySqlPath = 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($env:RUOYI_DATASOURCE_PASSWORD)) {
    throw 'RUOYI_DATASOURCE_PASSWORD must be set in the current process.'
}

$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$baselineVersion = [version]'2.25.2'
Import-Module (Join-Path $projectRoot 'scripts\MallMigration.psm1') -Force
$migrationCatalog = @(Get-MallMigrationCatalog -MigrationRoot (Join-Path $projectRoot 'sql'))
$migrationCount = $migrationCatalog.Count
$baselineCount = @($migrationCatalog | Where-Object { $_.Version -le $baselineVersion }).Count
$database = 'mall_migration_test_fresh_' + (Get-Date -Format 'yyyyMMddHHmmss')
$baselineDatabase = $database + '_baseline'
if ($database -notmatch '^mall_migration_test_fresh_\d{14}$') {
    throw "Unsafe temporary database name: $database"
}
if ($baselineDatabase -notmatch '^mall_migration_test_fresh_\d{14}_baseline$') {
    throw "Unsafe temporary baseline database name: $baselineDatabase"
}

function Invoke-MySqlClient([string]$Sql, [switch]$WithoutDatabase, [string]$DatabaseName = $database) {
    $previousPassword = $env:MYSQL_PWD
    $env:MYSQL_PWD = $env:RUOYI_DATASOURCE_PASSWORD
    try {
        $arguments = @('--protocol=TCP', '--host=127.0.0.1', '--port=3306', '--user=root',
            '--default-character-set=utf8mb4', '--batch', '--skip-column-names')
        if (-not $WithoutDatabase) {
            $arguments += $DatabaseName
        }
        $arguments += @('--execute', $Sql)
        $result = & $MySqlPath @arguments 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "mysql.exe failed: $($result -join ' ')"
        }
        return ($result -join "`n").Trim()
    } finally {
        $env:MYSQL_PWD = $previousPassword
    }
}

try {
    Invoke-MySqlClient -WithoutDatabase -Sql "create database $database default character set utf8mb4 collate utf8mb4_general_ci; create database $baselineDatabase default character set utf8mb4 collate utf8mb4_general_ci;" | Out-Null
    $baseScript = (Resolve-Path (Join-Path $projectRoot 'sql\ry_20260417.sql')).Path.Replace('\', '/')
    $quartzScript = (Resolve-Path (Join-Path $projectRoot 'sql\quartz.sql')).Path.Replace('\', '/')
    Invoke-MySqlClient -Sql "set names utf8mb4; source $baseScript; source $quartzScript;" | Out-Null

    & (Join-Path $projectRoot 'scripts\invoke-db-migrations.ps1') -Database $database
    & (Join-Path $projectRoot 'scripts\invoke-db-migrations.ps1') -Database $database

    $state = Invoke-MySqlClient -Sql @'
select concat(
  (select count(*) from mall_schema_history),
  ',',
  (select sum(success=1) from mall_schema_history),
  ',',
  (select sum(success=0) from mall_schema_history),
  ',',
  (select count(*) from information_schema.columns
   where table_schema=database() and table_name='mall_order' and column_name='risk_status')
);
'@
    $expectedState = "$migrationCount,$migrationCount,0,1"
    if ($state -ne $expectedState) {
        throw "Unexpected fresh migration state: $state"
    }

    Invoke-MySqlClient -DatabaseName $baselineDatabase -Sql "set names utf8mb4; source $baseScript; source $quartzScript; create table mall_order (order_id bigint not null primary key) engine=InnoDB;" | Out-Null
    & (Join-Path $projectRoot 'scripts\invoke-db-migrations.ps1') -Database $baselineDatabase -BaselineVersion 2.25.2
    & (Join-Path $projectRoot 'scripts\invoke-db-migrations.ps1') -Database $baselineDatabase
    $baselineState = Invoke-MySqlClient -DatabaseName $baselineDatabase -Sql "select concat(count(*), ',', sum(success=1), ',', sum(type='BASELINE')) from mall_schema_history;"
    $expectedBaselineState = "$migrationCount,$migrationCount,$baselineCount"
    if ($baselineState -ne $expectedBaselineState) {
        throw "Unexpected baseline migration state: $baselineState"
    }

    Write-Host "PASS: fresh and baseline databases applied and replayed all migrations ($state / $baselineState)"
} finally {
    Invoke-MySqlClient -WithoutDatabase -Sql "drop database if exists $database; drop database if exists $baselineDatabase;" | Out-Null
}
