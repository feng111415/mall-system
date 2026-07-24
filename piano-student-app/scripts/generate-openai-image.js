const fs = require('fs')
const path = require('path')
const OpenAI = require('openai')

const args = process.argv.slice(2)

function readArg(name, fallback) {
  const prefix = `--${name}=`
  const item = args.find(value => value.startsWith(prefix))
  return item ? item.slice(prefix.length) : fallback
}

function readPrompt() {
  const promptArg = readArg('prompt', '')
  if (promptArg) return promptArg
  return [
    'Create a soft minimalist piano-themed hero banner for a WeChat mini program.',
    'Warm milk-white background, low-saturation dusty pink accents, delicate five-line staff pattern, partial piano keys, subtle paper texture.',
    'Elegant calm literary music school feeling, suitable for piano teacher and parents.',
    'No readable text, no logo, no watermark, no people, no black heavy shadows.',
    'Leave clean negative space in the lower left for UI text overlay.'
  ].join(' ')
}

async function main() {
  if (!process.env.OPENAI_API_KEY) {
    console.error('缺少 OPENAI_API_KEY。请先在系统环境变量中配置，不要把 Key 写进代码。')
    console.error('PowerShell 当前窗口临时配置示例：$env:OPENAI_API_KEY="你的Key"')
    console.error('Windows 永久配置示例：setx OPENAI_API_KEY "你的Key"')
    process.exit(1)
  }

  const model = readArg('model', 'gpt-image-2')
  const size = readArg('size', '1536x1024')
  const quality = readArg('quality', 'medium')
  const output = readArg('out', 'static/generated/piano-hero-ai.png')
  const prompt = readPrompt()
  const baseURL = readArg('base-url', process.env.OPENAI_BASE_URL || '')

  const clientOptions = {
    apiKey: process.env.OPENAI_API_KEY
  }
  if (baseURL) {
    clientOptions.baseURL = baseURL
  }

  const client = new OpenAI(clientOptions)

  console.log(`[generate:image] model=${model} size=${size} quality=${quality}`)
  if (baseURL) {
    console.log(`[generate:image] baseURL=${baseURL}`)
  }

  const response = await client.images.generate({
    model,
    prompt,
    size,
    quality,
    output_format: 'png',
    background: 'opaque'
  })

  const image = response.data && response.data[0]
  if (!image || !image.b64_json) {
    throw new Error('OpenAI 未返回图片 base64 数据')
  }

  const outputPath = path.resolve(process.cwd(), output)
  fs.mkdirSync(path.dirname(outputPath), { recursive: true })
  fs.writeFileSync(outputPath, Buffer.from(image.b64_json, 'base64'))
  console.log(`[generate:image] saved ${outputPath}`)
}

main().catch(error => {
  console.error(error.message || error)
  process.exit(1)
})
