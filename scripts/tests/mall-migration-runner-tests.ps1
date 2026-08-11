$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

Import-Module (Join-Path (Split-Path -Parent $PSScriptRoot) 'MallMigration.psm1') -Force

function Assert-Equal($Expected, $Actual, [string]$Message) {
    if ($Expected -ne $Actual) {
        throw "$Message Expected=[$Expected] Actual=[$Actual]"
    }
}

function Assert-Throws([scriptblock]$Action, [string]$MessagePattern) {
    try {
        & $Action
    } catch {
        if ($_.Exception.Message -notmatch $MessagePattern) {
            throw "Unexpected error: $($_.Exception.Message)"
        }
        return
    }
    throw "Expected an error matching: $MessagePattern"
}

$fixtureRoot = Join-Path ([IO.Path]::GetTempPath()) ("mall-migration-tests-" + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $fixtureRoot | Out-Null
try {
    Set-Content -LiteralPath (Join-Path $fixtureRoot 'V2.9.0__older.sql') -Encoding UTF8 -Value 'select 1;'
    Set-Content -LiteralPath (Join-Path $fixtureRoot 'V2.10.0__newer.sql') -Encoding UTF8 -Value 'select 2;'
    Set-Content -LiteralPath (Join-Path $fixtureRoot 'V2.10.0__newer_rollback.sql') -Encoding UTF8 -Value 'select 3;'

    $catalog = @(Get-MallMigrationCatalog -MigrationRoot $fixtureRoot)
    Assert-Equal 2 $catalog.Count 'Rollback scripts must be excluded.'
    Assert-Equal '2.9.0' $catalog[0].VersionText 'Versions must use semantic ordering.'
    Assert-Equal '2.10.0' $catalog[1].VersionText 'Versions must use semantic ordering.'

    $history = @([pscustomobject]@{
        VersionText = $catalog[0].VersionText
        Script = $catalog[0].Script
        Checksum = $catalog[0].Checksum
        Type = 'SQL'
        Success = $true
    })
    $pending = @(Test-MallMigrationHistory -Catalog $catalog -History $history)
    Assert-Equal 1 $pending.Count 'Only unapplied versions should be pending.'
    Assert-Equal '2.10.0' $pending[0].VersionText 'The newer version should be pending.'

    $changedHistory = @([pscustomobject]@{
        VersionText = $catalog[0].VersionText
        Script = $catalog[0].Script
        Checksum = ('0' * 64)
        Type = 'SQL'
        Success = $true
    })
    Assert-Throws { Test-MallMigrationHistory -Catalog $catalog -History $changedHistory } 'checksum changed'

    $failedHistory = @([pscustomobject]@{
        VersionText = $catalog[0].VersionText
        Script = $catalog[0].Script
        Checksum = $catalog[0].Checksum
        Type = 'SQL'
        Success = $false
    })
    Assert-Throws { Test-MallMigrationHistory -Catalog $catalog -History $failedHistory } 'previously failed'

    Set-Content -LiteralPath (Join-Path $fixtureRoot 'V2.9.0__duplicate.sql') -Encoding UTF8 -Value 'select 4;'
    Assert-Throws { Get-MallMigrationCatalog -MigrationRoot $fixtureRoot } 'Duplicate migration versions'

    Assert-Equal "'O''Brien'" (ConvertTo-MallSqlLiteral "O'Brien") 'SQL literals must escape apostrophes.'
    Write-Host 'PASS: mall migration runner tests'
} finally {
    Remove-Item -LiteralPath $fixtureRoot -Recurse -Force
}
