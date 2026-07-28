$ErrorActionPreference = 'Stop'
$api = 'http://127.0.0.1:8080/api/mall'
$web = 'http://127.0.0.1:5174'
$n = 0
function Pass([string]$name, [string]$detail = '') { $script:n++; if ($detail) { Write-Host ("PASS {0}. {1} - {2}" -f $script:n, $name, $detail) -ForegroundColor Green } else { Write-Host ("PASS {0}. {1}" -f $script:n, $name) -ForegroundColor Green } }
function Api([string]$method, [string]$url, [hashtable]$headers = @{}, $body = $null) {
  $p = @{ Method = $method; Uri = $url; Headers = $headers; UseBasicParsing = $true }
  if ($null -ne $body) { $p.ContentType = 'application/json'; $p.Body = ($body | ConvertTo-Json -Depth 10 -Compress) }
  try { return Invoke-RestMethod @p } catch { $r = $_.Exception.Response; $detail = if ($r) { (New-Object IO.StreamReader($r.GetResponseStream())).ReadToEnd() } else { $_.Exception.Message }; throw "$method $url failed: $detail" }
}
foreach ($route in @('/', '/catalog', '/account', '/cart', '/checkout', '/orders')) { $page = Invoke-WebRequest -UseBasicParsing -Uri ($web + $route); if ($page.StatusCode -ne 200) { throw "storefront route $route returned $($page.StatusCode)" } }
Pass 'storefront routes' '6 routes returned HTTP 200'
$health = Api 'GET' "$api/health"; if ($health.code -ne 200 -or $health.data.status -ne 'UP') { throw 'health check failed' }; Pass 'backend health'
$unauthCart = Api 'GET' "$api/cart"; if ($unauthCart.code -ne 401) { throw 'anonymous cart access was not rejected' }; Pass 'anonymous access guard' 'cart requires login'
$categories = Api 'GET' "$api/catalog/categories"; $products = Api 'GET' "$api/catalog/products"
if ($categories.code -ne 200 -or @($categories.data).Count -eq 0) { throw 'category list is empty' }; if ($products.code -ne 200 -or @($products.data).Count -eq 0) { throw 'product list is empty' }
$product = @($products.data)[0]; $detail = Api 'GET' "$api/catalog/products/$($product.spuId)"; $sku = @($detail.data.skuList)[0]
if (-not $sku -or -not $sku.skuId) { throw 'product detail has no SKU' }; Pass 'catalog and sku' "SPU=$($product.spuCode), SKU=$($sku.skuCode)"
$phone = '139' + (Get-Random -Minimum 10000000 -Maximum 99999999); $send = Api 'POST' "$api/member/sms-code" @{} @{ phone = $phone }
if ($send.code -ne 200) { throw 'SMS send failed' }; $login = Api 'POST' "$api/member/login" @{} @{ phone = $phone; code = '123456'; agreed = $true; userAgreementVersion = '1.0'; privacyPolicyVersion = '1.0' }
$token = $login.data.token; if ($login.code -ne 200 -or [string]::IsNullOrWhiteSpace($token)) { throw 'login failed' }; $auth = @{ Authorization = "Bearer $token" }
$profile = Api 'GET' "$api/member/profile" $auth; if ($profile.code -ne 200 -or $profile.data.memberId -le 0) { throw 'profile failed' }; Pass 'sms login' $phone
$cartAdd = Api 'POST' "$api/cart/items" $auth @{ skuId = $sku.skuId; quantity = 1 }; $cartAddAgain = Api 'POST' "$api/cart/items" $auth @{ skuId = $sku.skuId; quantity = 1 }; $cartSelect = Api 'PUT' "$api/cart/items/$($sku.skuId)/selected" $auth @{ selected = $true }; $cart = Api 'GET' "$api/cart" $auth
$cartItem = @($cart.data.items | Where-Object { $_.skuId -eq $sku.skuId })[0]
if ($cartAdd.code -ne 200 -or $cartAddAgain.code -ne 200 -or $cartSelect.code -ne 200 -or $cart.code -ne 200 -or -not $cartItem -or $cartItem.quantity -ne 2 -or -not $cart.data.canCheckout) { throw 'cart repeated-click handling failed' }; Pass 'cart repeated add and select' "quantity=$($cartItem.quantity)"
$beforeAddress = Api 'GET' "$api/checkout/preview" $auth; if ($beforeAddress.code -ne 200 -or @($beforeAddress.data.addresses).Count -ne 0) { throw 'unexpected address state before add' }; Pass 'checkout without address' 'blocked as expected'
$address = Api 'POST' "$api/member/addresses" $auth @{ receiverName = 'E2E User'; receiverPhone = $phone; province = 'Beijing'; city = 'Beijing'; district = 'Chaoyang'; detailAddress = 'Test Street 1'; postalCode = '100000'; isDefault = '1' }
$addressId = $address.data.addressId; $addresses = Api 'GET' "$api/member/addresses" $auth
if ($address.code -ne 200 -or $addressId -le 0 -or $addresses.code -ne 200 -or @($addresses.data).Count -eq 0) { throw 'address add/list failed' }; Pass 'address add and list' "id=$addressId"
$preview = Api 'GET' "$api/checkout/preview" $auth; if ($preview.code -ne 200 -or $preview.data.defaultAddressId -ne $addressId -or -not $preview.data.canSubmit) { throw 'checkout preview is not submittable' }; Pass 'checkout preview' "amount=$($preview.data.payableAmount)"
$orderKey = [guid]::NewGuid().ToString(); $order = Api 'POST' "$api/orders" $auth @{ idempotencyKey = $orderKey; addressId = $addressId; remark = 'E2E test order' }; $orderId = $order.data.orderId
$replay = Api 'POST' "$api/orders" $auth @{ idempotencyKey = $orderKey; addressId = $addressId; remark = 'E2E test order' }
if ($order.code -ne 200 -or $orderId -le 0 -or $replay.data.orderId -ne $orderId) { throw 'order create/idempotency failed' }; Pass 'order create and replay' "order=$($order.data.orderNo)"
$payment = Api 'POST' "$api/orders/$orderId/payment" $auth @{ idempotencyKey = [guid]::NewGuid().ToString() }; $paymentNo = $payment.data.paymentNo; $paid = Api 'POST' "$api/payments/$paymentNo/mock-success" $auth
if ($payment.code -ne 200 -or [string]::IsNullOrWhiteSpace($paymentNo) -or $paid.code -ne 200 -or $paid.data.status -ne 'SUCCESS') { throw 'payment failed' }; Pass 'payment and mock success' $paymentNo
$orderDetail = Api 'GET' "$api/orders/$orderId" $auth; $orders = Api 'GET' "$api/orders?limit=50" $auth; $listed = @($orders.data | Where-Object { $_.orderId -eq $orderId }).Count
if ($orderDetail.code -ne 200 -or $orderDetail.data.status -notin @('PENDING_SHIPMENT', 'PAID') -or $orders.code -ne 200 -or $listed -eq 0) { throw 'order query failed' }; Pass 'order detail and list' "status=$($orderDetail.data.status)"
Write-Host 'E2E PASS: login -> catalog -> cart -> address -> checkout -> order -> payment' -ForegroundColor Cyan
