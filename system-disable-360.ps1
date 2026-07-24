$ErrorActionPreference = 'Continue'

$logDir = Join-Path $env:USERPROFILE 'Desktop\browser-cleanup-backup'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$logFile = Join-Path $logDir ("system-disable-360-" + (Get-Date -Format 'yyyyMMdd-HHmmss') + ".log")
Start-Transcript -Path $logFile -Force | Out-Null

Write-Host "Running as: $([Security.Principal.WindowsIdentity]::GetCurrent().Name)"

$runDeletes = @(
    'reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v 360sd /f',
    'reg delete "HKLM\Software\Microsoft\Windows\CurrentVersion\Run" /v 360sd /f',
    'reg delete "HKLM\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Run" /v 360Safetray /f',
    'reg delete "HKLM\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Run" /v 360TptMon.exe /f',
    'reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v 360DrvMgr /f',
    'reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v 360huabao /f'
)
foreach ($cmd in $runDeletes) {
    Write-Host $cmd
    cmd.exe /c $cmd
}

$serviceNames = @('360rp', 'ZhuDongFangYu', 'Q360AMPPL')
foreach ($name in $serviceNames) {
    Write-Host "Disable service: $name"
    cmd.exe /c "sc stop $name"
    cmd.exe /c "sc config $name start= disabled"
}

Get-ScheduledTask -ErrorAction SilentlyContinue |
    Where-Object { $_.TaskName -match '360|Qihoo|Qihu' -or $_.TaskPath -match '360|Qihoo|Qihu' } |
    ForEach-Object {
        Write-Host "Disable task: $($_.TaskPath)$($_.TaskName)"
        Disable-ScheduledTask -TaskName $_.TaskName -TaskPath $_.TaskPath -ErrorAction SilentlyContinue | Out-Null
    }

$shortcutTargets = @(
    "$env:USERPROFILE\Desktop\360安全卫士.lnk",
    "$env:USERPROFILE\Desktop\360软件管家.lnk",
    "$env:PUBLIC\Desktop\360驱动大师.lnk",
    "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\强力卸载电脑中的软件.lnk"
)
foreach ($target in $shortcutTargets) {
    Remove-Item -LiteralPath $target -Force -ErrorAction SilentlyContinue
}

Get-Process -ErrorAction SilentlyContinue |
    Where-Object { $_.ProcessName -match '^360|browprom|Qihoo|Qihu' -or $_.Path -match '\\360\\|360se6|360huabao' } |
    ForEach-Object {
        Write-Host "Stop process: $($_.ProcessName) $($_.Id)"
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    }

Stop-Transcript | Out-Null
