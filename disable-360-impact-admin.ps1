$ErrorActionPreference = 'Continue'

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host 'Please run this script as Administrator.'
    pause
    exit 1
}

$logDir = Join-Path $env:USERPROFILE 'Desktop\browser-cleanup-backup'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$logFile = Join-Path $logDir ("disable-360-impact-" + (Get-Date -Format 'yyyyMMdd-HHmmss') + ".log")
Start-Transcript -Path $logFile -Force | Out-Null

Write-Host 'Remove 360 startup registry entries'
$startupEntries = @(
    @{ Key = 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Run'; Name = '360sd' },
    @{ Key = 'HKLM:\Software\Microsoft\Windows\CurrentVersion\Run'; Name = '360sd' },
    @{ Key = 'HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Run'; Name = '360Safetray' },
    @{ Key = 'HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Run'; Name = '360TptMon.exe' },
    @{ Key = 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Run'; Name = '360DrvMgr' },
    @{ Key = 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Run'; Name = '360huabao' }
)
foreach ($entry in $startupEntries) {
    if (Test-Path $entry.Key) {
        Remove-ItemProperty -LiteralPath $entry.Key -Name $entry.Name -Force -ErrorAction SilentlyContinue
    }
}

Write-Host 'Disable 360 services'
$serviceNames = @('360rp', 'ZhuDongFangYu', 'Q360AMPPL')
foreach ($name in $serviceNames) {
    sc.exe stop $name | Out-Null
    sc.exe config $name start= disabled | Out-Null
}

Write-Host 'Disable 360 scheduled tasks'
Get-ScheduledTask -ErrorAction SilentlyContinue |
    Where-Object { $_.TaskName -match '360|Qihoo|Qihu' -or $_.TaskPath -match '360|Qihoo|Qihu' } |
    ForEach-Object {
        Disable-ScheduledTask -TaskName $_.TaskName -TaskPath $_.TaskPath -ErrorAction SilentlyContinue | Out-Null
    }

Write-Host 'Remove visible 360 shortcuts'
$shortcutTargets = @(
    "$env:USERPROFILE\Desktop\360安全卫士.lnk",
    "$env:USERPROFILE\Desktop\360软件管家.lnk",
    "$env:PUBLIC\Desktop\360驱动大师.lnk",
    "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\强力卸载电脑中的软件.lnk"
)
foreach ($target in $shortcutTargets) {
    Remove-Item -LiteralPath $target -Force -ErrorAction SilentlyContinue
}

Write-Host 'Block common 360 user-launched executables with IFEO'
$ifeoRoot = 'HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Image File Execution Options'
$exeNames = @(
    '360sdrun.exe',
    '360sd.exe',
    '360tray.exe',
    '360Safe.exe',
    'SoftMgr.exe',
    'DrvMgr.exe',
    '360zipUpdate.exe',
    '360secore.exe',
    '360seupdate.exe',
    'browprom64.exe'
)
foreach ($exe in $exeNames) {
    $key = Join-Path $ifeoRoot $exe
    New-Item -Path $key -Force | Out-Null
    New-ItemProperty -Path $key -Name 'Debugger' -PropertyType String -Value "$env:SystemRoot\System32\cmd.exe /c exit" -Force | Out-Null
}

Write-Host 'Stop currently running 360 processes'
Get-Process -ErrorAction SilentlyContinue |
    Where-Object { $_.ProcessName -match '^360|browprom|Qihoo|Qihu' -or $_.Path -match '\\360\\|360se6|360huabao' } |
    ForEach-Object {
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    }

Stop-Transcript | Out-Null
Write-Host "Done. Log: $logFile"
