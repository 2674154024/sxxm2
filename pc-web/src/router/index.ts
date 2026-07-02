import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/enterprise/register',
    name: 'EnterpriseRegister',
    component: () => import('@/views/enterprise/register/index.vue'),
    meta: { title: '企业注册' },
  },
  {
    path: '/enterprise',
    name: 'EnterpriseLayout',
    component: () => import('@/layouts/EnterpriseLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'EnterpriseDashboard',
        component: () => import('@/views/enterprise/dashboard/index.vue'),
        meta: { title: '数据看板' },
      },
      {
        path: 'job/list',
        name: 'JobList',
        component: () => import('@/views/enterprise/job/list.vue'),
        meta: { title: '岗位列表' },
      },
      {
        path: 'job/publish',
        name: 'JobPublish',
        component: () => import('@/views/enterprise/job/publish.vue'),
        meta: { title: '发布岗位' },
      },
      {
        path: 'job/edit',
        name: 'JobEdit',
        component: () => import('@/views/enterprise/job/edit.vue'),
        meta: { title: '编辑岗位' },
      },
      {
        path: 'job/detail',
        name: 'JobDetail',
        component: () => import('@/views/enterprise/job/detail.vue'),
        meta: { title: '岗位详情' },
      },
      {
        path: 'job/batch-import',
        name: 'JobBatchImport',
        component: () => import('@/views/enterprise/job/batch-import.vue'),
        meta: { title: '批量导入' },
      },
      {
        path: 'talent/apply',
        name: 'ApplyList',
        component: () => import('@/views/enterprise/talent/apply.vue'),
        meta: { title: '投递管理' },
      },
      {
        path: 'talent/library',
        name: 'TalentLibrary',
        component: () => import('@/views/enterprise/talent/library.vue'),
        meta: { title: '人才库' },
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/enterprise/schedule/index.vue'),
        meta: { title: '排班管理' },
      },
      {
        path: 'salary/calculate',
        name: 'SalaryCalculate',
        component: () => import('@/views/enterprise/salary/calculate.vue'),
        meta: { title: '薪资核算' },
      },
      {
        path: 'salary/records',
        name: 'PaymentRecords',
        component: () => import('@/views/enterprise/salary/records.vue'),
        meta: { title: '发放记录' },
      },
      {
        path: 'finance/statement',
        name: 'Statement',
        component: () => import('@/views/enterprise/finance/statement.vue'),
        meta: { title: '对账单' },
      },
      {
        path: 'finance/invoice',
        name: 'Invoice',
        component: () => import('@/views/enterprise/finance/invoice.vue'),
        meta: { title: '发票管理' },
      },
      {
        path: 'agreement',
        name: 'Agreement',
        component: () => import('@/views/enterprise/agreement/index.vue'),
        meta: { title: '协议管理' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/enterprise/settings/index.vue'),
        meta: { title: '系统设置' },
      },
    ],
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      {
        path: 'audit/enterprise',
        name: 'EnterpriseAudit',
        component: () => import('@/views/admin/audit/enterprise-list.vue'),
        meta: { title: '企业审核' },
      },
      {
        path: 'audit/job',
        name: 'JobAudit',
        component: () => import('@/views/admin/audit/job-list.vue'),
        meta: { title: '岗位审核' },
      },
      {
        path: 'risk/complaint',
        name: 'ComplaintList',
        component: () => import('@/views/admin/risk/complaint-list.vue'),
        meta: { title: '投诉工单' },
      },
      {
        path: 'risk/dashboard',
        name: 'RiskDashboard',
        component: () => import('@/views/admin/risk/dashboard.vue'),
        meta: { title: '风控看板' },
      },
      {
        path: 'finance/settlement',
        name: 'SettlementList',
        component: () => import('@/views/admin/finance/settlement-list.vue'),
        meta: { title: '结算管理' },
      },
      {
        path: 'finance/report',
        name: 'FinanceReport',
        component: () => import('@/views/admin/finance/report.vue'),
        meta: { title: '财务报表' },
      },
      {
        path: 'operation/report',
        name: 'OperationReport',
        component: () => import('@/views/admin/operation/report.vue'),
        meta: { title: '运营报表' },
      },
      {
        path: 'operation/notification',
        name: 'Notification',
        component: () => import('@/views/admin/operation/notification.vue'),
        meta: { title: '通知管理' },
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/admin/system/config.vue'),
        meta: { title: '系统配置' },
      },
      {
        path: 'system/roles',
        name: 'RoleManagement',
        component: () => import('@/views/admin/system/roles.vue'),
        meta: { title: '角色权限' },
      },
      {
        path: 'system/audit-log',
        name: 'AuditLog',
        component: () => import('@/views/admin/system/audit-log.vue'),
        meta: { title: '审计日志' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const title = (to.meta?.title as string) || ''
  if (to.path === '/') {
    document.title = '登录 - 长沙大学生兼职平台'
  } else if (to.path.startsWith('/admin')) {
    document.title = `${title} - 管理后台 - 长沙大学生兼职平台`
  } else if (to.path.startsWith('/enterprise')) {
    document.title = `${title} - 企业运营端 - 长沙大学生兼职平台`
  }

  const publicPaths = ['/', '/enterprise/register']
  
  if (publicPaths.includes(to.path)) {
    next()
    return
  }

  if (to.path.startsWith('/admin')) {
    const token = localStorage.getItem('admin_token')
    const roleType = localStorage.getItem('admin_role_type')
    if (!token || !roleType || roleType === '0') {
      next('/')
      return
    }
    next()
  } else if (to.path.startsWith('/enterprise')) {
    const token = localStorage.getItem('enterprise_token')
    const roleType = localStorage.getItem('enterprise_role_type')
    if (!token || !roleType || roleType !== '0') {
      next('/')
      return
    }
    next()
  } else {
    next()
  }
})

export default router