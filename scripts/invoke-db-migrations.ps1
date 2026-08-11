param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Za-z0-9_]+$')]
    [string]$Database,
    [ValidatePattern('^[A-Za-z0-9_.:-]+$')]
    [string]$DatabaseHost = '127.0.0.1',
    [ValidateRange(1, 65535)]
    [int]$Port = 3306,
    [ValidatePattern('^[A-Za-z0-9_.@-]+$')]
    [string]$Username = 'root',
    [string]$MigrationRoot,
    [string]$MySqlPath,
    [ValidatePattern('^\d+\.\d+\.\d+$')]
    [string]$BaselineVersion,
    [ValidateRange(1, 300)]
    [int]$LockTimeoutSeconds = 30,
    [ValidateRange(60, 86400)]
    [int]$LockLeaseSeconds = 3600
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($MigrationRoot)) {
    $packagedMigrationRoot = Join-Path $PSScriptRoot 'migrations'
    if (Test-Path -LiteralPath $packagedMigrationRoot) {
        $MigrationRoot = $packagedMigrationRoot
    } else {
        $MigrationRoot = Join-Path $projectRoot 'sql'
    }
}
if ([string]::IsNullOrWhiteSpace($MySqlPath)) {
    $command = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($command) {
        $MySqlPath = $command.Source
    } else {
        $candidate = Join-Path $env:ProgramFiles 'MySQL\MySQL Server 8.4\bin\mysql.exe'
        if (Test-Path -LiteralPath $candidate) {
            $MySqlPath = $candidate
        } else {
            throw 'mysql.exe was not found. Pass -MySqlPath explicitly.'
        }
    }
}

$password = [Environment]::GetEnvironmentVariable('RUOYI_DATASOURCE_PASSWORD', 'Process')
if ([string]::IsNullOrWhiteSpace($password)) {
    throw 'RUOYI_DATASOURCE_PASSWORD must be set in the current process.'
}

Import-Module (Join-Path $PSScriptRoot 'MallMigration.psm1') -Force
$catalog = @(Get-MallMigrationCatalog -MigrationRoot $MigrationRoot)
if ($catalog.Count -eq 0) {
    throw "No versioned migrations found in $MigrationRoot"
}

function Quote-ProcessArgument([string]$Value) {
    return '"' + $Value.Replace('"', '\"') + '"'
}

function New-MySqlStartInfo {
    $arguments = @(
        '--protocol=TCP',
        '--default-character-set=utf8mb4',
        '--batch',
        '--skip-column-names',
        '--raw',
        "--host=$DatabaseHost",
        "--port=$Port",
        "--user=$Username",
        $Database
    )

    $startInfo = New-Object Diagnostics.ProcessStartInfo
    $startInfo.FileName = $MySqlPath
    $startInfo.Arguments = ($arguments | ForEach-Object { Quote-ProcessArgument $_ }) -join ' '
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.CreateNoWindow = $true
    $startInfo.EnvironmentVariables['MYSQL_PWD'] = $password
    return $startInfo
}

function Invoke-MySql {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Sql
    )

    $process = New-Object Diagnostics.Process
    $process.StartInfo = New-MySqlStartInfo
    if (-not $process.Start()) {
        throw 'Failed to start mysql.exe.'
    }
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $process.StandardInput.Write($Sql)
    $process.StandardInput.Close()
    $process.WaitForExit()
    $stdout = $stdoutTask.Result.Trim()
    $stderr = $stderrTask.Result.Trim()
    if ($process.ExitCode -ne 0) {
        throw "mysql.exe failed with exit code $($process.ExitCode): $stderr"
    }
    return $stdout
}

$historyTableSql = @'
create table if not exists mall_schema_history (
  installed_rank int not null auto_increment,
  version varchar(50) not null,
  description varchar(200) not null,
  type varchar(20) not null,
  script varchar(255) not null,
  checksum char(64) not null,
  installed_by varchar(100) not null,
  installed_on timestamp not null default current_timestamp,
  execution_time_ms int not null default 0,
  success tinyint(1) not null default 0,
  primary key (installed_rank),
  unique key uk_mall_schema_history_version (version)
) engine=InnoDB default charset=utf8mb4 comment='mall migration history';
'@

$lockTableSql = @'
create table if not exists mall_schema_lock (
  lock_name varchar(100) not null,
  owner_id varchar(64) not null,
  locked_at timestamp not null default current_timestamp,
  expires_at timestamp not null,
  primary key (lock_name)
) engine=InnoDB default charset=utf8mb4 comment='mall migration lease lock';
'@

$lockName = "mall-schema-migration:$Database"
$lockLiteral = ConvertTo-MallSqlLiteral $lockName
$lockOwner = [guid]::NewGuid().ToString('N')
$lockOwnerLiteral = ConvertTo-MallSqlLiteral $lockOwner
$lockAcquired = $false

function Update-MigrationLockLease {
    $currentOwner = Invoke-MySql -Sql "update mall_schema_lock set expires_at=date_add(current_timestamp, interval $LockLeaseSeconds second) where lock_name=$lockLiteral and owner_id=$lockOwnerLiteral; select owner_id from mall_schema_lock where lock_name=$lockLiteral;"
    if ($currentOwner -ne $lockOwner) {
        throw "Migration lock was lost for database $Database."
    }
}

try {
    Invoke-MySql -Sql $lockTableSql | Out-Null
    $lockTimer = [Diagnostics.Stopwatch]::StartNew()
    do {
        $currentOwner = Invoke-MySql -Sql "insert into mall_schema_lock(lock_name,owner_id,locked_at,expires_at) values ($lockLiteral,$lockOwnerLiteral,current_timestamp,date_add(current_timestamp, interval $LockLeaseSeconds second)) on duplicate key update owner_id=if(expires_at < current_timestamp, values(owner_id), owner_id), locked_at=if(expires_at < current_timestamp, values(locked_at), locked_at), expires_at=if(expires_at < current_timestamp, values(expires_at), expires_at); select owner_id from mall_schema_lock where lock_name=$lockLiteral;"
        if ($currentOwner -eq $lockOwner) {
            $lockAcquired = $true
            break
        }
        if ($lockTimer.Elapsed.TotalSeconds -ge $LockTimeoutSeconds) {
            throw "Could not acquire migration lock for database $Database."
        }
        Start-Sleep -Seconds 1
    } while ($true)

    Invoke-MySql -Sql $historyTableSql | Out-Null

    $historyRows = Invoke-MySql -Sql @'
select concat_ws(char(9), version, script, checksum, type, success)
from mall_schema_history
order by installed_rank;
'@
    $history = @()
    if (-not [string]::IsNullOrWhiteSpace($historyRows)) {
        $history = @($historyRows -split "`r?`n" | ForEach-Object {
            $parts = $_ -split "`t", 5
            [pscustomobject]@{
                VersionText = $parts[0]
                Script = $parts[1]
                Checksum = $parts[2]
                Type = $parts[3]
                Success = $parts[4] -eq '1'
            }
        })
    }

    if ($history.Count -eq 0) {
        $mallTableCount = [int](Invoke-MySql -Sql @'
select count(*)
from information_schema.tables
where table_schema = database()
  and table_name like 'mall\_%'
  and table_name not in ('mall_schema_history', 'mall_schema_lock');
'@)
        if ($mallTableCount -gt 0 -and [string]::IsNullOrWhiteSpace($BaselineVersion)) {
            throw "Database $Database already has mall tables but no migration history. Pass -BaselineVersion only after verifying the schema."
        }
        if ($mallTableCount -eq 0 -and -not [string]::IsNullOrWhiteSpace($BaselineVersion)) {
            throw "Cannot baseline empty database $Database; omit -BaselineVersion to run all migrations."
        }

        if (-not [string]::IsNullOrWhiteSpace($BaselineVersion)) {
            $baseline = [version]$BaselineVersion
            if (-not ($catalog | Where-Object Version -eq $baseline)) {
                throw "Baseline version $BaselineVersion does not exist in the migration catalog."
            }
            $baselineMigrations = @($catalog | Where-Object Version -le $baseline)
            $baselineValues = @($baselineMigrations | ForEach-Object {
                $migration = $_
                "({0},{1},'BASELINE',{2},{3},{4},0,1)" -f
                    (ConvertTo-MallSqlLiteral $migration.VersionText),
                    (ConvertTo-MallSqlLiteral $migration.Description),
                    (ConvertTo-MallSqlLiteral $migration.Script),
                    (ConvertTo-MallSqlLiteral $migration.Checksum),
                    (ConvertTo-MallSqlLiteral $Username)
            })
            $baselineSql = "insert into mall_schema_history(version,description,type,script,checksum,installed_by,execution_time_ms,success) values " + ($baselineValues -join ',') + ';'
            Invoke-MySql -Sql $baselineSql | Out-Null
            Write-Host "Baseline recorded through version $BaselineVersion ($($baselineMigrations.Count) migrations)."
            $history = @($baselineMigrations | ForEach-Object {
                [pscustomobject]@{
                    VersionText = $_.VersionText
                    Script = $_.Script
                    Checksum = $_.Checksum
                    Type = 'BASELINE'
                    Success = $true
                }
            })
        }
    }

    $pending = @(Test-MallMigrationHistory -Catalog $catalog -History $history)
    if ($pending.Count -eq 0) {
        Write-Host "Database $Database is up to date at version $($catalog[-1].VersionText)."
        return
    }

    foreach ($migration in $pending) {
        Update-MigrationLockLease
        Write-Host "Applying $($migration.Script)..."
        $insertSql = "insert into mall_schema_history(version,description,type,script,checksum,installed_by,execution_time_ms,success) values ({0},{1},'SQL',{2},{3},{4},0,0);" -f
            (ConvertTo-MallSqlLiteral $migration.VersionText),
            (ConvertTo-MallSqlLiteral $migration.Description),
            (ConvertTo-MallSqlLiteral $migration.Script),
            (ConvertTo-MallSqlLiteral $migration.Checksum),
            (ConvertTo-MallSqlLiteral $Username)
        Invoke-MySql -Sql $insertSql | Out-Null

        $stopwatch = [Diagnostics.Stopwatch]::StartNew()
        try {
            $sourcePath = $migration.Path.Replace('\', '/')
            if ($sourcePath -match "[`r`n]") {
                throw "Migration path contains an invalid newline: $sourcePath"
            }
            Invoke-MySql -Sql "set names utf8mb4;`r`nsource $sourcePath;" | Out-Null
            $stopwatch.Stop()
            Invoke-MySql -Sql "update mall_schema_history set execution_time_ms=$($stopwatch.ElapsedMilliseconds), success=1 where version=$(ConvertTo-MallSqlLiteral $migration.VersionText);" | Out-Null
        } catch {
            $stopwatch.Stop()
            Invoke-MySql -Sql "update mall_schema_history set execution_time_ms=$($stopwatch.ElapsedMilliseconds) where version=$(ConvertTo-MallSqlLiteral $migration.VersionText);" | Out-Null
            throw
        }
    }

    Write-Host "Applied $($pending.Count) migration(s). Current version: $($pending[-1].VersionText)."
} finally {
    if ($lockAcquired) {
        try {
            Invoke-MySql -Sql "delete from mall_schema_lock where lock_name=$lockLiteral and owner_id=$lockOwnerLiteral;" | Out-Null
        } catch {
            Write-Warning "Migration lease release failed; it will expire automatically: $($_.Exception.Message)"
        }
    }
}
