import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Index',
    component: () => import('@/views/index/index.vue'),
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
    meta: { title: '投递记录', showTabBar: true, requireAuth: true },
  },
  {
    path: '/message',
    name: 'Message',
    component: () => import('@/views/message/index.vue'),
    meta: { title: '消息', showTabBar: true, requireAuth: true },
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/message/chat.vue'),
    meta: { title: '聊天', showTabBar: false, requireAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { title: '个人中心', showTabBar: true },
  },
  {
    path: '/profile/edit',
    name: 'ProfileEdit',
    component: () => import('@/views/profile/edit.vue'),
    meta: { title: '编辑个人资料', showTabBar: false, requireAuth: true },
  },
  {
    path: '/profile/verify',
    name: 'ProfileVerify',
    component: () => import('@/views/profile/verify.vue'),
    meta: { title: '实名认证', showTabBar: false, requireAuth: true },
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
    path: '/case',
    name: 'CaseList',
    component: () => import('@/views/case/index.vue'),
    meta: { title: '典型案例', showTabBar: false },
  },
  {
    path: '/case/:id',
    name: 'CaseDetail',
    component: () => import('@/views/case/detail.vue'),
    meta: { title: '案例详情', showTabBar: false },
  },
  {
    path: '/complaint/create',
    name: 'ComplaintCreate',
    component: () => import('@/views/complaint/create.vue'),
    meta: { title: '投诉举报', showTabBar: false, requireAuth: true },
  },
  {
    path: '/complaint/list',
    name: 'ComplaintList',
    component: () => import('@/views/complaint/list.vue'),
    meta: { title: '我的投诉', showTabBar: false, requireAuth: true },
  },
  {
    path: '/resume',
    name: 'Resume',
    component: () => import('@/views/resume/edit.vue'),
    meta: { title: '我的简历', showTabBar: false, requireAuth: true },
  },
  {
    path: '/favorite',
    name: 'Favorite',
    component: () => import('@/views/favorite/index.vue'),
    meta: { title: '我的收藏', showTabBar: false, requireAuth: true },
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('@/views/feedback/index.vue'),
    meta: { title: '意见反馈', showTabBar: false, requireAuth: true },
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/about/index.vue'),
    meta: { title: '关于我们', showTabBar: false },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  if (to.meta?.title) {
    document.title = to.meta.title as string
  }
  
  const token = localStorage.getItem('token')
  if (to.meta?.requireAuth && !token) {
    next('/login')
    return
  }
  
  next()
})

export default router
