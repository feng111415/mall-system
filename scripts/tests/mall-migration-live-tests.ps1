param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^mall_migration_test_[A-Za-z0-9_]+$')]
    [string]$Database,
    [string]$MySqlPath = 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($env:RUOYI_DATASOURCE_PASSWORD)) {
    throw 'RUOYI_DATASOURCE_PASSWORD must be set in the current process.'
}

$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$fixtureRoot = Join-Path ([IO.Path]::GetTempPath()) ('mall-migration-live-' + [guid]::NewGuid().ToString('N'))
$probeScript = Join-Path $fixtureRoot 'V2.25.3__migration_runner_probe.sql'
$failedScript = Join-Path $fixtureRoot 'V2.25.4__migration_runner_failure_probe.sql'
$runner = Join-Path $projectRoot 'scripts\invoke-db-migrations.ps1'
$packagedDatabase = Join-Path $fixtureRoot 'packaged\database'
$packagedMigrations = Join-Path $packagedDatabase 'migrations'
$testLockOwner = 'mall-migration-live-test-holder'

function Invoke-TestMySql([string]$Sql) {
    $previousPassword = $env:MYSQL_PWD
    $env:MYSQL_PWD = $env:RUOYI_DATASOURCE_PASSWORD
    try {
        $result = & $MySqlPath --protocol=TCP --host=127.0.0.1 --port=3306 --user=root `
            --default-character-set=utf8mb4 --batch --skip-column-names $Database --execute $Sql 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "mysql.exe failed: $($result -join ' ')"
        }
        return ($result -join "`n").Trim()
    } finally {
        $env:MYSQL_PWD = $previousPassword
    }
}

New-Item -ItemType Directory -Path $fixtureRoot | Out-Null
try {
    Get-ChildItem (Join-Path $projectRoot 'sql') -File -Filter 'V*.sql' |
        Copy-Item -Destination $fixtureRoot
    New-Item -ItemType Directory -Path $packagedMigrations -Force | Out-Null
    Get-ChildItem (Join-Path $projectRoot 'sql') -File -Filter 'V*.sql' |
        Copy-Item -Destination $packagedMigrations
    Copy-Item -LiteralPath $runner -Destination $packagedDatabase
    Copy-Item -LiteralPath (Join-Path $projectRoot 'scripts\MallMigration.psm1') -Destination $packagedDatabase
    & (Join-Path $packagedDatabase 'invoke-db-migrations.ps1') -Database $Database

    Invoke-TestMySql "insert into mall_schema_lock(lock_name,owner_id,locked_at,expires_at) values ('mall-schema-migration:$Database','$testLockOwner',current_timestamp,date_add(current_timestamp, interval 60 second)) on duplicate key update owner_id=values(owner_id), locked_at=values(locked_at), expires_at=values(expires_at);" | Out-Null
    $concurrentRunRejected = $false
    try {
        & $runner -Database $Database -LockTimeoutSeconds 1 2>&1 | Out-Null
    } catch {
        $concurrentRunRejected = $_.Exception.Message -match 'Could not acquire migration lock'
    } finally {
        Invoke-TestMySql "delete from mall_schema_lock where lock_name='mall-schema-migration:$Database' and owner_id='$testLockOwner';" | Out-Null
    }
    if (-not $concurrentRunRejected) {
        throw 'Concurrent migration runner was not rejected.'
    }

    Set-Content -LiteralPath $probeScript -Encoding ASCII `
        -Value 'create table mall_migration_runner_probe (id int not null primary key) engine=InnoDB;'

    & $runner -Database $Database -MigrationRoot $fixtureRoot
    & $runner -Database $Database -MigrationRoot $fixtureRoot

    $probeState = Invoke-TestMySql @'
select concat(
  (select count(*) from information_schema.tables
   where table_schema=database() and table_name='mall_migration_runner_probe'),
  ',',
  (select count(*) from mall_schema_history where version='2.25.3' and success=1)
);
'@
    if ($probeState -ne '1,1') {
        throw "Probe migration did not settle exactly once: $probeState"
    }

    Add-Content -LiteralPath $probeScript -Encoding ASCII -Value '-- checksum drift'
    $driftRejected = $false
    $driftMessage = ''
    try {
        & $runner -Database $Database -MigrationRoot $fixtureRoot 2>&1 | Out-Null
    } catch {
        $driftMessage = $_.Exception.Message
        $driftRejected = $driftMessage -match 'checksum changed'
    }
    if (-not $driftRejected) {
        throw 'Checksum drift was not rejected.'
    }

    Set-Content -LiteralPath $probeScript -Encoding ASCII `
        -Value 'create table mall_migration_runner_probe (id int not null primary key) engine=InnoDB;'
    Set-Content -LiteralPath $failedScript -Encoding ASCII -Value 'this is not valid SQL;'
    $failedMigrationRejected = $false
    try {
        & $runner -Database $Database -MigrationRoot $fixtureRoot 2>&1 | Out-Null
    } catch {
        $failedMigrationRejected = $_.Exception.Message -match 'mysql.exe failed'
    }
    if (-not $failedMigrationRejected) {
        throw 'Invalid migration SQL was not rejected.'
    }
    $failedState = Invoke-TestMySql "select count(*) from mall_schema_history where version='2.25.4' and success=0;"
    if ($failedState -ne '1') {
        throw "Failed migration history was not retained: $failedState"
    }

    Write-Host 'PASS: packaged path, concurrent lock, replay, checksum drift, and failed history checks'
} finally {
    Invoke-TestMySql @'
drop table if exists mall_migration_runner_probe;
delete from mall_schema_history where version in ('2.25.3', '2.25.4');
'@ | Out-Null
    Invoke-TestMySql "delete from mall_schema_lock where lock_name='mall-schema-migration:$Database' and owner_id='$testLockOwner';" | Out-Null

    $resolvedFixture = (Resolve-Path -LiteralPath $fixtureRoot -ErrorAction SilentlyContinue).Path
    $resolvedTemp = (Resolve-Path -LiteralPath ([IO.Path]::GetTempPath())).Path
    if ($resolvedFixture -and $resolvedFixture.StartsWith($resolvedTemp, [StringComparison]::OrdinalIgnoreCase)) {
        Remove-Item -LiteralPath $resolvedFixture -Recurse -Force
    }
}
