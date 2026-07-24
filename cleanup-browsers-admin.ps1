$ErrorActionPreference = 'Continue'

function Remove-PathSafe {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string[]]$AllowedRoots
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }

    $item = Get-Item -LiteralPath $Path -Force
    $full = $item.FullName.TrimEnd('\')
    $allowed = $false
    foreach ($root in $AllowedRoots) {
        $resolvedRoot = (Get-Item -LiteralPath $root -Force).FullName.TrimEnd('\')
        if ($full.Equals($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase) -or
            $full.StartsWith($resolvedRoot + '\', [System.StringComparison]::OrdinalIgnoreCase)) {
            $allowed = $true
            break
        }
    }
    if (-not $allowed) {
        Write-Host "SKIP unsafe path: $full"
        return
    }

    Write-Host "Deleting: $full"
    cmd /c "takeown /F `"$full`" /R /D Y" | Out-Null
    cmd /c "icacls `"$full`" /grant Administrators:F /T /C" | Out-Null
    attrib -R -S -H "$full\*" /S /D 2>$null
    Remove-Item -LiteralPath $full -Recurse -Force -ErrorAction SilentlyContinue
    if (Test-Path -LiteralPath $full) {
        Write-Host "FAILED remains: $full"
    } else {
        Write-Host "DELETED: $full"
    }
}

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host 'Please run this script as Administrator.'
    pause
    exit 1
}

$logDir = Join-Path $env:USERPROFILE 'Desktop\browser-cleanup-backup'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$logFile = Join-Path $logDir ("cleanup-360-" + (Get-Date -Format 'yyyyMMdd-HHmmss') + ".log")
Start-Transcript -Path $logFile -Force | Out-Null

$patterns = '360|Qihoo|Qihu|browprom'

Get-Process -ErrorAction SilentlyContinue |
    Where-Object { $_.ProcessName -match $patterns -or $_.Path -match $patterns } |
    Where-Object { $_.ProcessName -notin @('Code') } |
    ForEach-Object {
        Write-Host "Stopping process: $($_.ProcessName) ($($_.Id))"
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    }

$services = @('2345Pic', '360rp', 'ZhuDongFangYu', 'Q360AMPPL')
foreach ($service in $services) {
    sc.exe stop $service | Out-Null
    sc.exe config $service start= disabled | Out-Null
    sc.exe delete $service | Out-Null
}

$tasks = Get-ScheduledTask -ErrorAction SilentlyContinue |
    Where-Object { $_.TaskName -match $patterns -or $_.TaskPath -match $patterns }
foreach ($task in $tasks) {
    Write-Host "Disabling task: $($task.TaskPath)$($task.TaskName)"
    Disable-ScheduledTask -TaskName $task.TaskName -TaskPath $task.TaskPath -ErrorAction SilentlyContinue | Out-Null
    Unregister-ScheduledTask -TaskName $task.TaskName -TaskPath $task.TaskPath -Confirm:$false -ErrorAction SilentlyContinue
}

$runKeys = @(
    'HKCU:\Software\Microsoft\Windows\CurrentVersion\Run',
    'HKLM:\Software\Microsoft\Windows\CurrentVersion\Run',
    'HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Run'
)
$runNames = @('360sd', '360DrvMgr', '360huabao', '360Safetray', '360TptMon.exe')
foreach ($key in $runKeys) {
    if (Test-Path $key) {
        foreach ($name in $runNames) {
            Remove-ItemProperty -LiteralPath $key -Name $name -ErrorAction SilentlyContinue
        }
    }
}

$userRoots = @($env:LOCALAPPDATA, $env:APPDATA, "$env:USERPROFILE\Desktop")
$programRoots = @('C:\Program Files', 'C:\Program Files (x86)', 'C:\ProgramData')
$deleteTargets = @(
    "$env:LOCALAPPDATA\360Chrome",
    "$env:APPDATA\360browser",
    "$env:APPDATA\360se6",
    "$env:APPDATA\360huabao",
    'C:\Program Files (x86)\360',
    'C:\ProgramData\360safe',
    'C:\ProgramData\360TotalSecurity'
)

foreach ($target in $deleteTargets) {
    Remove-PathSafe -Path $target -AllowedRoots ($userRoots + $programRoots)
}

$desktopLinks = @(
    "$env:USERPROFILE\Desktop\360安全卫士.lnk",
    "$env:USERPROFILE\Desktop\360软件管家.lnk",
    "$env:PUBLIC\Desktop\360驱动大师.lnk"
)
foreach ($link in $desktopLinks) {
    Remove-Item -LiteralPath $link -Force -ErrorAction SilentlyContinue
}

Write-Host ''
Write-Host 'Cleanup finished. Reboot Windows, then test Edge/Chrome.'
Stop-Transcript | Out-Null
Write-Host "Log: $logFile"
