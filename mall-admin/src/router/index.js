import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import ProductsView from '../views/ProductsView.vue'
import OrdersView from '../views/OrdersView.vue'
import MembersView from '../views/MembersView.vue'
import CatalogSettingsView from '../views/CatalogSettingsView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', component: DashboardView },
    { path: '/products', component: ProductsView },
    { path: '/catalog-settings', component: CatalogSettingsView },
    { path: '/members', component: MembersView },
    { path: '/orders', component: OrdersView }
  ]
})

router.beforeEach(to => {
  const hasToken = Boolean(sessionStorage.getItem('mall-admin-token'))
  if (!to.meta.public && !hasToken) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.path === '/login' && hasToken) return '/dashboard'
})

export default router
