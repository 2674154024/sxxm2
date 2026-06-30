import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Index',
    component: () => import('@/views/index/index.vue'),
<<<<<<< HEAD
    meta: { title: '长沙大学生兼职平台', showTabBar: true },
  },
  {
    path: '/job/:id',
    name: 'JobDetail',
    component: () => import('@/views/job/detail.vue'),
    meta: { title: '岗位详情', showTabBar: false },
  },
  {
    path: '/apply',
    name: 'Apply',
    component: () => import('@/views/apply/index.vue'),
    meta: { title: '投递记录', showTabBar: true },
  },
  {
    path: '/message',
    name: 'Message',
    component: () => import('@/views/message/index.vue'),
    meta: { title: '消息', showTabBar: true },
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/message/chat.vue'),
    meta: { title: '聊天', showTabBar: false },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { title: '个人中心', showTabBar: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', showTabBar: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', showTabBar: false },
  },
  {
    path: '/safety',
    name: 'Safety',
    component: () => import('@/views/safety/index.vue'),
    meta: { title: '防骗指南', showTabBar: false },
  },
  {
    path: '/resume',
    name: 'Resume',
    component: () => import('@/views/resume/edit.vue'),
    meta: { title: '我的简历', showTabBar: false },
  },
  {
    path: '/favorite',
    name: 'Favorite',
    component: () => import('@/views/favorite/index.vue'),
    meta: { title: '我的收藏', showTabBar: false },
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('@/views/feedback/index.vue'),
    meta: { title: '意见反馈', showTabBar: false },
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/about/index.vue'),
    meta: { title: '关于我们', showTabBar: false },
=======
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
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
<<<<<<< HEAD
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  if (to.meta?.title) {
    document.title = to.meta.title as string
  }
  next()
})

export default router
=======
})

export default router
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
