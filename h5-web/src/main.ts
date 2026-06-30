import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
<<<<<<< HEAD
import './styles/global.scss'
=======
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Vant)

app.mount('#app')