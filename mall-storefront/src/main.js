import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'vant/lib/index.css'
import './styles/variables.css'
import './styles/phase-two.css'

createApp(App).use(createPinia()).use(router).mount('#app')
