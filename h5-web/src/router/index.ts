import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Index',
    component: () => import('@/views/index/index.vue'),
  },
  {
    path: '/job/detail/:id',
    name: 'JobDetail',
    component: () => import('@/views/job/detail.vue'),
  },
  {
    path: '/mine',
    name: 'Mine',
    component: () => import('@/views/mine/index.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router