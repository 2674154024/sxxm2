import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { useAdminStore } from '@/stores/admin'
import { useEnterpriseStore } from '@/stores/enterprise'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

const adminStore = useAdminStore()
const enterpriseStore = useEnterpriseStore()
adminStore.init()
enterpriseStore.init()

app.mount('#app')