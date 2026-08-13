param(
    [int]$Port = 8080,
    [ValidateSet('cloudflared', 'ngrok')]
    [string]$Provider = 'cloudflared'
)

$ErrorActionPreference = 'Stop'

function Find-Executable([string]$Name) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    $workspaceTool = Join-Path (Split-Path $PSScriptRoot -Parent) "..\tools\$Name.exe"
    if (Test-Path -LiteralPath $workspaceTool) { return (Resolve-Path $workspaceTool).Path }
    throw "Executable not found: $Name. Install it and add it to PATH first."
}

if ($Port -lt 1 -or $Port -gt 65535) { throw "Port must be between 1 and 65535." }

switch ($Provider) {
    'cloudflared' {
        $executable = Find-Executable 'cloudflared'
        Write-Host "Starting Cloudflare temporary tunnel for http://127.0.0.1:$Port"
        & $executable tunnel --url "http://127.0.0.1:$Port"
    }
    'ngrok' {
        $executable = Find-Executable 'ngrok'
        Write-Host "Starting ngrok temporary tunnel for http://127.0.0.1:$Port"
        & $executable http $Port
    }
}
