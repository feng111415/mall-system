const fs = require('fs')
const path = require('path')

const loaderPath = path.join(
  __dirname,
  '..',
  'node_modules',
  '@dcloudio',
  'vue-cli-plugin-uni',
  'packages',
  'vue-loader',
  'lib',
  'loaders',
  'templateLoader.js'
)

if (!fs.existsSync(loaderPath)) {
  console.warn('[patch-dcloud-template-loader] templateLoader.js not found')
  process.exit(0)
}

const source = fs.readFileSync(loaderPath, 'utf8')
const expected = 'return code + `\\nexport { render, staticRenderFns, recyclableRender, components }`'
const previousBadPatch =
  "return code + `\\nvar recyclableRender = typeof recyclableRender === 'undefined' ? false : recyclableRender` +\\n    `\\nvar components = typeof components === 'undefined' ? undefined : components` +\\n    `\\nexport { render, staticRenderFns, recyclableRender, components }`"
const patched =
  "return code + `\\nvar recyclableRender = typeof recyclableRender === 'undefined' ? false : recyclableRender` +" +
  "\n    `\\nvar components = typeof components === 'undefined' ? undefined : components` +" +
  "\n    `\\nexport { render, staticRenderFns, recyclableRender, components }`"

if (source.includes(patched)) {
  console.log('[patch-dcloud-template-loader] already patched')
  process.exit(0)
}

if (source.includes(previousBadPatch)) {
  fs.writeFileSync(loaderPath, source.replace(previousBadPatch, patched), 'utf8')
  console.log('[patch-dcloud-template-loader] fixed previous patch')
  process.exit(0)
}

if (!source.includes(expected)) {
  console.warn('[patch-dcloud-template-loader] expected template not found')
  process.exit(0)
}

fs.writeFileSync(loaderPath, source.replace(expected, patched), 'utf8')
console.log('[patch-dcloud-template-loader] patched')
