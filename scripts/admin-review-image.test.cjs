const assert = require('assert')
const fs = require('fs')
const path = require('path')

const source = fs.readFileSync(path.resolve(__dirname, '../ruoyi-ui/src/views/mall/review/index.vue'), 'utf8')
const match = source.match(/imageUrl \(value\) \{([^\r\n]*)\}/)
if (!match) throw new Error('未找到评价页 imageUrl 方法')
const imageUrl = new Function('value', match[1])
const previousBase = process.env.VUE_APP_BASE_API
process.env.VUE_APP_BASE_API = '/dev-api'

try {
  assert.strictEqual(imageUrl('/assets/lamp.jpg'), '/assets/lamp.jpg', '商城静态素材不能请求后台 API')
  assert.strictEqual(imageUrl('/profile/mall/review/1/demo.png'), '/dev-api/profile/mall/review/1/demo.png', '上传文件应通过后台 API')
  assert.strictEqual(imageUrl('https://cdn.example.com/review.png'), 'https://cdn.example.com/review.png', '外链应保持原样')
} finally {
  if (previousBase === undefined) delete process.env.VUE_APP_BASE_API
  else process.env.VUE_APP_BASE_API = previousBase
}

console.log('后台评价图片路径回归测试通过：静态素材、上传文件和外链。')
