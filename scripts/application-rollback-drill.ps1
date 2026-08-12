param(
    [string]$BackendJar = 'ruoyi-admin\target\ruoyi-admin.jar',
    [string]$StorefrontDist = 'mall-storefront\dist',
    [string]$AdminDist = 'ruoyi-ui\dist',
    [int]$Port = 18080
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$drillRoot = Join-Path $env:TEMP ("ruoyi-rollback-drill-" + [guid]::NewGuid().ToString('N'))
$deployRoot = Join-Path $drillRoot 'deploy'
$backupRoot = Join-Path $drillRoot 'backup'
$logPath = Join-Path $drillRoot 'backend.log'
$pidPath = Join-Path $drillRoot 'backend.pid'

function Assert-TemporaryTarget([string]$path)
{
    $resolvedTemp = [IO.Path]::GetFullPath($env:TEMP).TrimEnd('\') + '\'
    $resolvedPath = [IO.Path]::GetFullPath($path).TrimEnd('\') + '\'
    if (-not $resolvedPath.StartsWith($resolvedTemp, [StringComparison]::OrdinalIgnoreCase))
    {
        throw "Rollback drill target must stay inside the system temporary directory: $path"
    }
}

function Start-Backend([string]$jarPath)
{
    $arguments = @(
        '-jar', $jarPath,
        "--server.port=$Port",
        '--spring.quartz.auto-startup=false'
    )
    $process = Start-Process -FilePath 'java' -ArgumentList $arguments -WorkingDirectory $deployRoot `
        -WindowStyle Hidden -RedirectStandardOutput $logPath -PassThru
    Set-Content -LiteralPath $pidPath -Value $process.Id -Encoding ASCII
    return $process
}

function Stop-Backend
{
    if (-not (Test-Path -LiteralPath $pidPath)) { return }
    $processId = [int](Get-Content -LiteralPath $pidPath)
    Stop-Process -Id $processId -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
}

function Wait-Health([int]$seconds = 35)
{
    $deadline = (Get-Date).AddSeconds($seconds)
    do
    {
        Start-Sleep -Seconds 1
        try
        {
            $response = Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:$Port/api/mall/health" -TimeoutSec 2
            if ($response.StatusCode -eq 200) { return $true }
        }
        catch { }
    }
    while ((Get-Date) -lt $deadline)
    return $false
}

Assert-TemporaryTarget $drillRoot
New-Item -ItemType Directory -Force -Path $deployRoot, $backupRoot | Out-Null
$backendSource = Join-Path $projectRoot $BackendJar
$storefrontSource = Join-Path $projectRoot $StorefrontDist
$adminSource = Join-Path $projectRoot $AdminDist
foreach ($source in @($backendSource, $storefrontSource, $adminSource))
{
    if (-not (Test-Path -LiteralPath $source)) { throw "Rollback drill source missing: $source" }
}

try
{
    Copy-Item -LiteralPath $backendSource -Destination (Join-Path $deployRoot 'ruoyi-admin.jar')
    Copy-Item -LiteralPath $storefrontSource -Destination (Join-Path $deployRoot 'mall-storefront') -Recurse
    Copy-Item -LiteralPath $adminSource -Destination (Join-Path $deployRoot 'ruoyi-ui') -Recurse
    $initialStorefrontHash = (Get-FileHash -LiteralPath (Join-Path $deployRoot 'mall-storefront\index.html')).Hash
    $initialAdminHash = (Get-FileHash -LiteralPath (Join-Path $deployRoot 'ruoyi-ui\index.html')).Hash

    Start-Backend (Join-Path $deployRoot 'ruoyi-admin.jar') | Out-Null
    if (-not (Wait-Health)) { throw 'Initial rollback-drill deployment did not become healthy' }
    Write-Host 'PASS 1. initial deployment health' -ForegroundColor Green

    Stop-Backend
    Copy-Item -LiteralPath (Join-Path $deployRoot 'ruoyi-admin.jar') -Destination (Join-Path $backupRoot 'ruoyi-admin.jar')
    Copy-Item -LiteralPath (Join-Path $deployRoot 'mall-storefront') -Destination (Join-Path $backupRoot 'mall-storefront') -Recurse
    Copy-Item -LiteralPath (Join-Path $deployRoot 'ruoyi-ui') -Destination (Join-Path $backupRoot 'ruoyi-ui') -Recurse
    Write-Host 'PASS 2. current artifacts backed up' -ForegroundColor Green

    [IO.File]::WriteAllText((Join-Path $deployRoot 'ruoyi-admin.jar'), 'invalid release candidate')
    [IO.File]::WriteAllText((Join-Path $deployRoot 'mall-storefront\index.html'), 'invalid storefront candidate')
    [IO.File]::WriteAllText((Join-Path $deployRoot 'ruoyi-ui\index.html'), 'invalid admin candidate')
    $failedProcess = Start-Backend (Join-Path $deployRoot 'ruoyi-admin.jar')
    $failedProcess.WaitForExit(10000) | Out-Null
    if (Wait-Health 3) { throw 'Invalid candidate unexpectedly passed its health check' }
    Write-Host 'PASS 3. failed candidate detected by health check' -ForegroundColor Green

    Stop-Backend
    Remove-Item -LiteralPath (Join-Path $deployRoot 'ruoyi-admin.jar') -Force
    Remove-Item -LiteralPath (Join-Path $deployRoot 'mall-storefront') -Recurse -Force
    Remove-Item -LiteralPath (Join-Path $deployRoot 'ruoyi-ui') -Recurse -Force
    Copy-Item -LiteralPath (Join-Path $backupRoot 'ruoyi-admin.jar') -Destination (Join-Path $deployRoot 'ruoyi-admin.jar')
    Copy-Item -LiteralPath (Join-Path $backupRoot 'mall-storefront') -Destination (Join-Path $deployRoot 'mall-storefront') -Recurse
    Copy-Item -LiteralPath (Join-Path $backupRoot 'ruoyi-ui') -Destination (Join-Path $deployRoot 'ruoyi-ui') -Recurse
    Start-Backend (Join-Path $deployRoot 'ruoyi-admin.jar') | Out-Null
    if (-not (Wait-Health)) { throw 'Restored backend did not become healthy' }
    if ((Get-FileHash -LiteralPath (Join-Path $deployRoot 'mall-storefront\index.html')).Hash -ne $initialStorefrontHash)
    {
        throw 'Storefront artifact was not restored exactly'
    }
    if ((Get-FileHash -LiteralPath (Join-Path $deployRoot 'ruoyi-ui\index.html')).Hash -ne $initialAdminHash)
    {
        throw 'Admin artifact was not restored exactly'
    }
    Write-Host 'PASS 4. backend and frontend artifacts restored' -ForegroundColor Green
    Write-Host 'APPLICATION ROLLBACK DRILL PASS' -ForegroundColor Cyan
}
finally
{
    Stop-Backend
    if (Test-Path -LiteralPath $drillRoot)
    {
        Assert-TemporaryTarget $drillRoot
        Remove-Item -LiteralPath $drillRoot -Recurse -Force
    }
}
