const fs = require('fs')
const path = require('path')

const vendorPath = path.join(
  __dirname,
  '..',
  'dist',
  'build',
  'mp-weixin',
  'common',
  'vendor.js'
)

if (!fs.existsSync(vendorPath)) {
  console.warn('[patch-mp-weixin-vendor] vendor.js not found')
  process.exit(0)
}

const source = fs.readFileSync(vendorPath, 'utf8')
const i18nMarker = 'initVueI18n: () => (/* binding */ initVueI18n)'
const markerIndex = source.indexOf(i18nMarker)

if (markerIndex === -1) {
  console.error('[patch-mp-weixin-vendor] uni-i18n module marker not found')
  process.exit(1)
}

const moduleEnd = source.indexOf('/***/ })', markerIndex)
const moduleSource = source.slice(markerIndex, moduleEnd === -1 ? source.length : moduleEnd)
const patchedLine =
  '/* patched dependency */ var uni = undefined;'
const providedDependencyPattern =
  /\/\* provided dependency \*\/ var uni = __webpack_require__\(\d+\)\["default"\];/

if (moduleSource.includes(patchedLine)) {
  console.log('[patch-mp-weixin-vendor] already patched')
  process.exit(0)
}

const match = moduleSource.match(providedDependencyPattern)
if (!match) {
  console.error('[patch-mp-weixin-vendor] uni-i18n provided dependency not found')
  process.exit(1)
}

const absoluteStart = markerIndex + match.index
const absoluteEnd = absoluteStart + match[0].length
const patched =
  source.slice(0, absoluteStart) +
  patchedLine +
  source.slice(absoluteEnd)

fs.writeFileSync(vendorPath, patched, 'utf8')
console.log('[patch-mp-weixin-vendor] patched uni-i18n circular dependency')
