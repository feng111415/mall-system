Page({
  data: { status: '' },
  onLoad(query) { this.setData({ status: query.status || '' }) }
})
