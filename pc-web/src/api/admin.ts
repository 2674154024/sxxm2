import request from './request'

export interface EnterpriseAuditItem {
  enterprise_id: string
  enterprise_name: string
  credit_code: string
  legal_person: string
  contact_name: string
  contact_phone: string
  business_license: string
  verify_status: number
  submit_time: string
  reject_reason: string
}

export interface JobAuditItem {
  job_id: string
  job_title: string
  enterprise_name: string
  enterprise_id: string
  salary_amount: number
  work_address: string
  status: number
  submit_time: string
  has_sensitive_word: boolean
  sensitive_words: string[]
}

export interface ComplaintItem {
  complaint_id: string
  complainant_id: string
  complainant_name: string
  defendant_id: string
  defendant_name: string
  defendant_type: number
  job_id: string
  job_title: string
  complaint_type: string
  complaint_content: string
  evidence_urls: string[]
  status: number
  create_time: string
  handle_result: string
}

export interface RiskStats {
  today_complaint_count: number
  pending_count: number
  frozen_amount: number
  compensated_amount: number
}

export interface SettlementItem {
  flow_id: string
  enterprise_name: string
  student_name: string
  job_title: string
  amount: number
  status: number
  create_time: string
}

export interface AuditLogItem {
  log_id: string
  operator_id: string
  operator_name: string
  role_type: number
  module: string
  action: string
  request_params: string
  ip_address: string
  client_type: string
  create_time: string
}

export interface AdminRoleItem {
  role_id: string
  role_name: string
  role_type: number
  permissions: string[]
  create_time: string
}

export interface AdminUserItem {
  admin_id: string
  username: string
  real_name: string
  phone: string
  role_type: number
  role_name: string
  status: number
  create_time: string
}

export const auditApi = {
  getEnterpriseAuditList(params: { page?: number; size?: number; status?: number }) {
    return request.get('/v1/pc/admin/enterprise/audit/list', { params })
  },

  auditEnterprise(data: { enterprise_id: string; action: 'pass' | 'reject'; reason?: string }) {
    return request.post('/v1/pc/admin/enterprise/audit', data)
  },

  getJobAuditList(params: { page?: number; size?: number; status?: number; keyword?: string }) {
    return request.get('/v1/pc/admin/job/audit/list', { params })
  },

  auditJob(data: { job_id: string; action: 'pass' | 'reject'; reason?: string }) {
    return request.post('/v1/pc/admin/job/audit', data)
  }
}

export const riskApi = {
  getComplaintList(params: { page?: number; size?: number; status?: number }) {
    return request.get('/v1/pc/admin/risk/complaint/list', { params })
  },

  handleComplaint(data: {
    complaint_id: string
    action: 'freeze' | 'unfreeze' | 'deduct' | 'compensate'
    amount?: number
    handle_result: string
  }) {
    return request.post('/v1/pc/admin/risk/complaint/handle', data)
  },

  getRiskDashboard() {
    return request.get('/v1/pc/admin/risk/dashboard')
  }
}

export const financeApi = {
  getSettlementList(params: { page?: number; size?: number; status?: number }) {
    return request.get('/v1/pc/admin/finance/settlement/list', { params })
  },

  batchPay(data: { flow_ids: string[] }) {
    return request.post('/v1/pc/admin/finance/settlement/pay', data)
  },

  getFinanceReport(params: { start_date?: string; end_date?: string }) {
    return request.get('/v1/pc/admin/finance/report', { params })
  }
}

export const operationApi = {
  getOperationReport(params: { start_date?: string; end_date?: string }) {
    return request.get('/v1/pc/admin/operation/report', { params })
  },

  sendNotification(data: {
    title: string
    content: string
    target_type: 'all' | 'student' | 'enterprise'
    target_ids?: string[]
  }) {
    return request.post('/v1/pc/admin/operation/notification', data)
  }
}

export const systemApi = {
  getSystemConfig() {
    return request.get('/v1/pc/admin/system/config')
  },

  updateSystemConfig(data: {
    min_hourly_wage?: number
    max_hourly_wage?: number
    skill_weight?: number
    time_weight?: number
    distance_weight?: number
    credit_score_rules?: string
  }) {
    return request.put('/v1/pc/admin/system/config', data)
  },

  getAdminList(params: { page?: number; size?: number }) {
    return request.get('/v1/pc/admin/system/admin/list', { params })
  },

  createAdmin(data: {
    username: string
    password: string
    real_name: string
    phone: string
    role_type: number
  }) {
    return request.post('/v1/pc/admin/system/admin', data)
  },

  updateAdmin(data: {
    admin_id: string
    real_name?: string
    phone?: string
    role_type?: number
    status?: number
  }) {
    return request.put('/v1/pc/admin/system/admin', data)
  },

  deleteAdmin(admin_id: string) {
    return request.delete(`/v1/pc/admin/system/admin/${admin_id}`)
  },

  getRoleList() {
    return request.get('/v1/pc/admin/system/role/list')
  },

  getRolePermissions(role_id: string) {
    return request.get(`/v1/pc/admin/system/role/permissions/${role_id}`)
  },

  updateRolePermissions(data: { role_id: string; permissions: string[] }) {
    return request.put('/v1/pc/admin/system/role/permissions', data)
  },

  getAuditLogList(params: {
    page?: number
    size?: number
    role_type?: number
    module?: string
    start_time?: string
    end_time?: string
  }) {
    return request.get('/v1/pc/admin/audit-log/list', { params })
  }
}

export const authApi = {
  adminLogin(data: { username: string; password: string }) {
    return request.post('/v1/pc/admin/login', data)
  }
}