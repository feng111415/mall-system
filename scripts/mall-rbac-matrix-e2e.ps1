param(
    [string]$BaseUrl = 'http://127.0.0.1:8080',
    [int]$RedisPort = 28555
)

$ErrorActionPreference = 'Stop'
$password = $env:RUOYI_RBAC_TEST_PASSWORD
if ([string]::IsNullOrWhiteSpace($password))
{
    throw 'RUOYI_RBAC_TEST_PASSWORD must be injected for the RBAC test accounts'
}
$redisCli = 'D:\360Downloads\redis-cli.exe'
if (-not (Test-Path $redisCli)) { throw "redis-cli not found: $redisCli" }

function Invoke-Api([string]$method, [string]$url, [hashtable]$headers = @{}, $body = $null)
{
    $request = [Net.HttpWebRequest]::Create($url)
    $request.Method = $method
    $request.ContentType = 'application/json'
    foreach ($entry in $headers.GetEnumerator())
    {
        if ($entry.Key -eq 'Authorization') { $request.Headers.Add([Net.HttpRequestHeader]::Authorization, [string]$entry.Value) }
        else { $request.Headers[$entry.Key] = [string]$entry.Value }
    }
    if ($null -ne $body)
    {
        $payload = [Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Depth 10 -Compress))
        $request.ContentLength = $payload.Length
        $stream = $request.GetRequestStream()
        try { $stream.Write($payload, 0, $payload.Length) } finally { $stream.Dispose() }
    }
    try
    {
        $response = $request.GetResponse()
    }
    catch [Net.WebException]
    {
        if (-not $_.Exception.Response) { throw }
        $response = $_.Exception.Response
    }
    $status = [int]$response.StatusCode
    $reader = New-Object IO.StreamReader($response.GetResponseStream())
    try { $content = $reader.ReadToEnd() } finally { $reader.Dispose(); $response.Dispose() }
    $json = if ([string]::IsNullOrWhiteSpace($content)) { $null } else { $content | ConvertFrom-Json }
    return [pscustomobject]@{ Status = $status; Body = $json }
}

function Login([string]$username)
{
    $captcha = Invoke-Api 'GET' "$BaseUrl/captchaImage"
    if ($captcha.Status -ne 200 -or -not $captcha.Body.uuid) { throw "$username captcha request failed" }
    $rawCode = & $redisCli -p $RedisPort --raw GET ("captcha_codes:" + $captcha.Body.uuid)
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($rawCode)) { throw "$username captcha lookup failed" }
    try { $code = $rawCode | ConvertFrom-Json } catch { $code = $rawCode.Trim('"') }
    $login = Invoke-Api 'POST' "$BaseUrl/login" @{} @{
        username = $username
        password = $password
        code = [string]$code
        uuid = $captcha.Body.uuid
    }
    if ($login.Status -ne 200 -or $login.Body.code -ne 200 -or [string]::IsNullOrWhiteSpace($login.Body.token))
    {
        throw "$username login failed"
    }
    return [string]$login.Body.token
}

$roles = @(
    @{ User = 'ops_lead_test'; Role = 'mall_ops_lead'; Sections = @('sales','product','member','fulfillment','afterSale','finance'); Access = @{ Product=200; AfterSale=200; Inventory=200; Logistics=200; Finance=200 } },
    @{ User = 'product_ops_test'; Role = 'mall_product_ops'; Sections = @('sales','product'); Access = @{ Product=200; AfterSale=403; Inventory=200; Logistics=403; Finance=403 } },
    @{ User = 'customer_serV_test'; Role = 'mall_customer_service'; Sections = @('member','afterSale'); Access = @{ Product=403; AfterSale=200; Inventory=403; Logistics=200; Finance=400 } },
    @{ User = 'fulfillment_test'; Role = 'mall_fulfillment'; Sections = @('fulfillment'); Access = @{ Product=403; AfterSale=403; Inventory=200; Logistics=200; Finance=403 } },
    @{ User = 'finance_risk_test'; Role = 'mall_finance_risk'; Sections = @('sales','finance'); Access = @{ Product=403; AfterSale=200; Inventory=403; Logistics=403; Finance=200 } }
)

$endpoints = @{
    Product = '/mall/catalog/products?pageNum=1&pageSize=1'
    AfterSale = '/mall/after-sale/item-orders'
    Inventory = '/mall/inventory/list?pageNum=1&pageSize=1'
    Logistics = '/mall/logistics/summary'
    Finance = '/mall/after-sale-funds/center?tab=DIFF&pageNum=1&pageSize=1'
}

$count = 0
foreach ($role in $roles)
{
    $token = Login $role.User
    $headers = @{ Authorization = "Bearer $token" }
    $info = Invoke-Api 'GET' "$BaseUrl/getInfo" $headers
    $routers = Invoke-Api 'GET' "$BaseUrl/getRouters" $headers
    $actualRoles = @($info.Body.roles)
    if ($info.Status -ne 200 -or $info.Body.code -ne 200 -or $actualRoles.Count -ne 1 -or $actualRoles[0] -ne $role.Role)
    {
        throw "$($role.User) returned an unexpected role"
    }
    if ($routers.Status -ne 200 -or $routers.Body.code -ne 200 -or @($routers.Body.data).Count -eq 0)
    {
        throw "$($role.User) has no router tree"
    }
    $permissions = @($info.Body.permissions)
    $mustNotHaveFundsCenter = $role.User -in @('product_ops_test', 'fulfillment_test')
    $hasFundsCenter = $permissions -contains 'mall:after-sale-funds:center:query'
    if ($mustNotHaveFundsCenter -and $hasFundsCenter)
    {
        throw "$($role.User) unexpectedly has mall:after-sale-funds:center:query"
    }

    $analytics = Invoke-Api 'GET' "$BaseUrl/mall/business-analytics?days=30" $headers
    if ($analytics.Status -ne 200 -or $analytics.Body.code -ne 200)
    {
        throw "$($role.User) analytics request failed"
    }
    $actualSections = @($analytics.Body.data.sections | Sort-Object)
    $expectedSections = @($role.Sections | Sort-Object)
    if (($actualSections -join ',') -ne ($expectedSections -join ','))
    {
        throw "$($role.User) analytics sections were outside its responsibilities"
    }

    foreach ($capability in $endpoints.Keys)
    {
        $result = Invoke-Api 'GET' ($BaseUrl + $endpoints[$capability]) $headers
        $expectedStatus = [int]$role.Access[$capability]
        if ($result.Status -ne $expectedStatus)
        {
            throw "$($role.User) $capability expected HTTP $expectedStatus, got $($result.Status)"
        }
    }
    $count++
    Write-Host "PASS $count. $($role.User) -> $($role.Role), sections=$($expectedSections -join ',')" -ForegroundColor Green
}
Write-Host 'RBAC MATRIX E2E PASS' -ForegroundColor Cyan
