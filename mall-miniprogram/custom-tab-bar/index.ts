Component({
  data: {
    selected: 0,
    hidden: false,
    list: [
      { pagePath: '/pages/home/index', text: '首页', icon: '/assets/icons/house.svg' },
      { pagePath: '/pages/catalog/index', text: '分类', icon: '/assets/icons/layout-grid.svg' },
      { pagePath: '/pages/cart/index', text: '购物车', icon: '/assets/icons/shopping-cart.svg' },
      { pagePath: '/pages/messages/index', text: '消息', icon: '/assets/icons/message-circle.svg' },
      { pagePath: '/pages/profile/index', text: '我的', icon: '/assets/icons/circle-user-round.svg' }
    ]
  },
  attached() {
    this.updateSelected()
  },
  pageLifetimes: {
    show() {
      this.updateSelected()
    }
  },
  methods: {
    updateSelected() {
      const pages = getCurrentPages()
      const current = pages[pages.length - 1]
      const selected = this.data.list.findIndex(item => `/${current.route}` === item.pagePath)
      this.setData({ selected: selected < 0 ? 0 : selected })
    },
    switchTab(event: WechatMiniprogram.BaseEvent) {
      const { path } = event.currentTarget.dataset
      wx.switchTab({ url: path })
    }
  }
})
