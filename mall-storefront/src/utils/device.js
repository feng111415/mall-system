const DEVICE_ID_KEY = 'mall-device-id'
let memoryDeviceId = ''

function createDeviceId() {
  if (globalThis.crypto?.randomUUID) return globalThis.crypto.randomUUID().replaceAll('-', '')
  const bytes = new Uint8Array(16)
  globalThis.crypto.getRandomValues(bytes)
  return Array.from(bytes, value => value.toString(16).padStart(2, '0')).join('')
}

export function getDeviceId() {
  if (/^[a-f0-9]{32}$/.test(memoryDeviceId)) return memoryDeviceId
  try {
    const stored = localStorage.getItem(DEVICE_ID_KEY)
    if (/^[a-f0-9]{32}$/.test(stored || '')) return (memoryDeviceId = stored)
    memoryDeviceId = createDeviceId()
    localStorage.setItem(DEVICE_ID_KEY, memoryDeviceId)
    return memoryDeviceId
  } catch {
    return (memoryDeviceId ||= createDeviceId())
  }
}
