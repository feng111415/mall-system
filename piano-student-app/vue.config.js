module.exports = {
  configureWebpack: config => {
    if (process.env.UNI_PLATFORM === 'mp-weixin') {
      config.optimization = config.optimization || {}
      config.optimization.minimize = false
      config.optimization.concatenateModules = false
    }
  }
}
