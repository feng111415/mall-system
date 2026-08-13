const productUtils = require('./product')

function createIdempotencyKey() {
  let random = ''
  while (random.length < 24) random += Math.floor(Math.random() * 0x100000000).toString(16).padStart(8, '0')
  return `mp-order-${Date.now()}-${random.slice(0, 24)}`
}

function normalizeAddress(address: Record<string, any>) {
  return {
    ...address,
    displayReceiver: `${address.receiverName || ''} ${address.receiverPhone || ''}`.trim(),
    displayAddress: [address.province, address.city, address.district, address.detailAddress].filter(Boolean).join(' '),
    defaultFlag: address.isDefault === '1'
  }
}

function normalizeCheckout(value: Record<string, any> | null | undefined) {
  const addresses = (value?.addresses || []).map(normalizeAddress)
  const items = (value?.items || []).map((item: Record<string, any>) => ({
    ...item,
    displayImage: productUtils.localProductImage(item.productImage),
    displayUnitPrice: productUtils.formatPrice(item.unitPrice),
    displayLineAmount: productUtils.formatPrice(item.lineAmount)
  }))
  const coupons = (value?.coupons || []).filter((item: Record<string, any>) => item.status === 'AVAILABLE').map((item: Record<string, any>) => ({
    ...item,
    displayDiscount: productUtils.formatPrice(item.discountAmount),
    displayThreshold: productUtils.formatPrice(item.thresholdAmount)
  }))
  const defaultAddressId = Number(value?.defaultAddressId || addresses[0]?.addressId || 0)
  return {
    items,
    addresses,
    coupons,
    defaultAddressId,
    selectedMemberCouponId: Number(value?.selectedMemberCouponId || 0),
    couponName: value?.couponName || '',
    productAmount: productUtils.formatPrice(value?.productAmount),
    shippingFee: productUtils.formatPrice(value?.shippingFee),
    discountAmount: productUtils.formatPrice(value?.discountAmount),
    payableAmount: productUtils.formatPrice(value?.payableAmount),
    canSubmit: Boolean(value?.canSubmit),
    validationMessages: value?.validationMessages || [],
    itemCount: items.reduce((total: number, item: Record<string, any>) => total + Math.max(0, Number(item.quantity || 0)), 0)
  }
}

module.exports = { createIdempotencyKey, normalizeAddress, normalizeCheckout }
