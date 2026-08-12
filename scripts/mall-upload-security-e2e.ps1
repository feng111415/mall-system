$ErrorActionPreference = 'Stop'
$api = 'http://127.0.0.1:8080/api/mall'
$deviceId = [guid]::NewGuid().ToString('N')
$phone = '136' + (Get-Random -Minimum 10000000 -Maximum 99999999)
$headers = @{
    'X-Mall-Device-Id' = $deviceId
    'X-Forwarded-For' = '203.0.113.89'
}

function Invoke-JsonApi([string]$method, [string]$url, [hashtable]$requestHeaders, $body)
{
    $request = [Net.HttpWebRequest]::Create($url)
    $request.Method = $method
    $request.ContentType = 'application/json'
    foreach ($entry in $requestHeaders.GetEnumerator())
    {
        $request.Headers[$entry.Key] = [string]$entry.Value
    }
    $payload = [Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Compress))
    $request.ContentLength = $payload.Length
    $stream = $request.GetRequestStream()
    try { $stream.Write($payload, 0, $payload.Length) } finally { $stream.Dispose() }
    try
    {
        $response = $request.GetResponse()
    }
    catch [Net.WebException]
    {
        if (-not $_.Exception.Response) { throw }
        $response = $_.Exception.Response
    }
    $reader = New-Object IO.StreamReader($response.GetResponseStream())
    try { $content = $reader.ReadToEnd() } finally { $reader.Dispose(); $response.Dispose() }
    return $content | ConvertFrom-Json
}

function Invoke-Upload([string]$url, [string]$path, [string]$token)
{
    $client = New-Object Net.Http.HttpClient
    $client.DefaultRequestHeaders.Add('X-Mall-Authorization', "Bearer $token")
    $form = New-Object Net.Http.MultipartFormDataContent
    $stream = [IO.File]::OpenRead($path)
    $part = New-Object Net.Http.StreamContent($stream)
    $part.Headers.ContentType = [Net.Http.Headers.MediaTypeHeaderValue]::Parse('image/png')
    $form.Add($part, 'file', [IO.Path]::GetFileName($path))
    try
    {
        $response = $client.PostAsync($url, $form).Result
        $content = $response.Content.ReadAsStringAsync().Result | ConvertFrom-Json
        return [pscustomobject]@{ Http = [int]$response.StatusCode; Body = $content }
    }
    finally
    {
        $stream.Dispose()
        $form.Dispose()
        $client.Dispose()
    }
}

Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.Net.Http
$testRoot = Join-Path $env:TEMP 'mall-upload-security-e2e'
New-Item -ItemType Directory -Force -Path $testRoot | Out-Null
$validPath = Join-Path $testRoot 'valid.png'
$forgedPath = Join-Path $testRoot 'forged.png'
$oversizePath = Join-Path $testRoot 'oversize.png'
$bitmap = New-Object Drawing.Bitmap 120, 80
$graphics = [Drawing.Graphics]::FromImage($bitmap)
try
{
    $graphics.Clear([Drawing.Color]::FromArgb(31, 111, 95))
    $bitmap.Save($validPath, [Drawing.Imaging.ImageFormat]::Png)
}
finally
{
    $graphics.Dispose()
    $bitmap.Dispose()
}
[IO.File]::WriteAllText($forgedPath, 'not-an-image', [Text.Encoding]::UTF8)
[IO.File]::WriteAllBytes($oversizePath, (New-Object byte[] (5 * 1024 * 1024 + 1)))

Invoke-JsonApi 'POST' "$api/member/sms-code" $headers @{ phone = $phone } | Out-Null
$login = Invoke-JsonApi 'POST' "$api/member/login" $headers @{
    phone = $phone
    code = '123456'
    agreed = $true
    userAgreementVersion = '1.0'
    privacyPolicyVersion = '1.0'
    deviceId = $deviceId
}
$token = $login.data.token
if ($login.code -ne 200 -or [string]::IsNullOrWhiteSpace($token)) { throw 'Upload E2E login failed' }

$cases = @(
    @{ Name = 'avatar valid image'; Url = "$api/member/profile/avatar/upload"; Path = $validPath; Http = 200; Code = 200; PngField = 'avatar' },
    @{ Name = 'avatar forged image'; Url = "$api/member/profile/avatar/upload"; Path = $forgedPath; Http = 400; Code = 400 },
    @{ Name = 'avatar oversized image'; Url = "$api/member/profile/avatar/upload"; Path = $oversizePath; Http = 400; Code = 400 },
    @{ Name = 'review valid image'; Url = "$api/reviews/images"; Path = $validPath; Http = 200; Code = 200; PngField = 'data' },
    @{ Name = 'review forged image'; Url = "$api/reviews/images"; Path = $forgedPath; Http = 400; Code = 400 },
    @{ Name = 'review oversized image'; Url = "$api/reviews/images"; Path = $oversizePath; Http = 400; Code = 400 }
)

$count = 0
foreach ($case in $cases)
{
    $result = Invoke-Upload $case.Url $case.Path $token
    if ($result.Http -ne $case.Http -or $result.Body.code -ne $case.Code)
    {
        throw "$($case.Name) expected HTTP/code $($case.Http)/$($case.Code), got $($result.Http)/$($result.Body.code)"
    }
    $storedUrl = if ($case.PngField -eq 'avatar') { $result.Body.data.avatar } else { $result.Body.data }
    if ($case.PngField -and $storedUrl -notmatch '/[0-9a-f]{32}\.png$')
    {
        throw "$($case.Name) was not stored as a random PNG"
    }
    $count++
    Write-Host "PASS $count. $($case.Name)" -ForegroundColor Green
}
Write-Host 'UPLOAD SECURITY E2E PASS' -ForegroundColor Cyan
