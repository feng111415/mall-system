Write-Host ""
Write-Host "OpenAI / Sub2API key setup" -ForegroundColor Cyan
Write-Host "The key will be saved to the current Windows user environment variables only." -ForegroundColor Gray
Write-Host "It will not be written into project source code." -ForegroundColor Gray
Write-Host ""

$secureKey = Read-Host "Paste API key here. Input is hidden" -AsSecureString
$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)

try {
    $plainKey = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    if ($plainKey -match 'OPENAI_API_KEY\s*=\s*"([^"]+)"') {
        $plainKey = $Matches[1]
    }
    $plainKey = $plainKey.Trim().Trim('"')

    if ([string]::IsNullOrWhiteSpace($plainKey)) {
        Write-Host "Empty key. Setup cancelled." -ForegroundColor Yellow
        exit 1
    }

    Write-Host ""
    Write-Host "For official OpenAI, press Enter on the next prompt." -ForegroundColor Gray
    Write-Host "For Sub2API, enter base URL, for example: https://api.nytokens.com/v1" -ForegroundColor Gray
    $baseUrl = Read-Host "API Base URL, optional"
    if ($baseUrl -match 'OPENAI_BASE_URL\s*=\s*"([^"]+)"') {
        $baseUrl = $Matches[1]
    }
    $baseUrl = $baseUrl.Trim().Trim('"')

    [Environment]::SetEnvironmentVariable("OPENAI_API_KEY", $plainKey, "User")
    $env:OPENAI_API_KEY = $plainKey

    if (-not [string]::IsNullOrWhiteSpace($baseUrl)) {
        [Environment]::SetEnvironmentVariable("OPENAI_BASE_URL", $baseUrl.Trim(), "User")
        $env:OPENAI_BASE_URL = $baseUrl.Trim()
    }

    Write-Host ""
    Write-Host "OPENAI_API_KEY saved." -ForegroundColor Green
    if (-not [string]::IsNullOrWhiteSpace($baseUrl)) {
        Write-Host "OPENAI_BASE_URL saved: $($baseUrl.Trim())" -ForegroundColor Green
    }
    Write-Host "Open a new PowerShell / VS Code / Codex session to read the new variables." -ForegroundColor Gray
    Write-Host ""
} finally {
    if ($bstr -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

Read-Host "Press Enter to close"
