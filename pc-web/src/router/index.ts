import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
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
      },
      {
        path: 'job/list',
        name: 'JobList',
        component: () => import('@/views/enterprise/job/list.vue'),
      },
      {
        path: 'job/publish',
        name: 'JobPublish',
        component: () => import('@/views/enterprise/job/publish.vue'),
      },
      {
        path: 'job/batch-import',
        name: 'JobBatchImport',
        component: () => import('@/views/enterprise/job/batch-import.vue'),
      },
      {
        path: 'talent/apply',
        name: 'ApplyList',
        component: () => import('@/views/enterprise/talent/apply.vue'),
      },
      {
        path: 'talent/library',
        name: 'TalentLibrary',
        component: () => import('@/views/enterprise/talent/library.vue'),
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/enterprise/schedule/index.vue'),
      },
      {
        path: 'salary/calculate',
        name: 'SalaryCalculate',
        component: () => import('@/views/enterprise/salary/calculate.vue'),
      },
      {
        path: 'salary/records',
        name: 'PaymentRecords',
        component: () => import('@/views/enterprise/salary/records.vue'),
      },
      {
        path: 'finance/statement',
        name: 'Statement',
        component: () => import('@/views/enterprise/finance/statement.vue'),
      },
      {
        path: 'finance/invoice',
        name: 'Invoice',
        component: () => import('@/views/enterprise/finance/invoice.vue'),
      },
      {
        path: 'agreement',
        name: 'Agreement',
        component: () => import('@/views/enterprise/agreement/index.vue'),
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/enterprise/settings/index.vue'),
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
      },
      {
        path: 'audit/job',
        name: 'JobAudit',
        component: () => import('@/views/admin/audit/job-list.vue'),
      },
      {
        path: 'risk/complaint',
        name: 'ComplaintList',
        component: () => import('@/views/admin/risk/complaint-list.vue'),
      },
      {
        path: 'risk/dashboard',
        name: 'RiskDashboard',
        component: () => import('@/views/admin/risk/dashboard.vue'),
      },
      {
        path: 'finance/settlement',
        name: 'SettlementList',
        component: () => import('@/views/admin/finance/settlement-list.vue'),
      },
      {
        path: 'finance/report',
        name: 'FinanceReport',
        component: () => import('@/views/admin/finance/report.vue'),
      },
      {
        path: 'operation/report',
        name: 'OperationReport',
        component: () => import('@/views/admin/operation/report.vue'),
      },
      {
        path: 'operation/notification',
        name: 'Notification',
        component: () => import('@/views/admin/operation/notification.vue'),
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/admin/system/config.vue'),
      },
      {
        path: 'system/roles',
        name: 'RoleManagement',
        component: () => import('@/views/admin/system/roles.vue'),
      },
      {
        path: 'system/audit-log',
        name: 'AuditLog',
        component: () => import('@/views/admin/system/audit-log.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/') {
    next()
  } else if (!token) {
    next('/')
  } else {
    const roleType = localStorage.getItem('roleType')
    if (to.path.startsWith('/admin') && !roleType) {
      next('/')
    } else if (to.path.startsWith('/enterprise') && roleType) {
      next()
    } else {
      next()
    }
  }
})

export default router