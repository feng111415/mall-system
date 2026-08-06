import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import CatalogView from '../views/CatalogView.vue'
import CartView from '../views/CartView.vue'
import AccountView from '../views/AccountView.vue'
import ProductDetailView from '../views/ProductDetailView.vue'
import CheckoutView from '../views/CheckoutView.vue'
import OrderListView from '../views/OrderListView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'
import MessageCenterView from '../views/MessageCenterView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/catalog', name: 'catalog', component: CatalogView },
    { path: '/product/:id', name: 'product-detail', component: ProductDetailView },
    { path: '/cart', name: 'cart', component: CartView },
    { path: '/checkout', name: 'checkout', component: CheckoutView },
    { path: '/orders', name: 'orders', component: OrderListView },
    { path: '/orders/:orderId', name: 'order-detail', component: OrderDetailView },
    { path: '/messages', name: 'messages', component: MessageCenterView },
    { path: '/account', name: 'account', component: AccountView }
  ],
  scrollBehavior: () => ({ top: 0 })
})

export default router
