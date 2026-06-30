import request from './request'

export interface Statement {
  statement_id: string
  month: string
  total_income: number
  total_payment: number
  balance: number
  status: 'pending' | 'confirmed' | 'paid'
  created_time: string
}

export interface Invoice {
  invoice_id: string
  type: 'vat' | 'ordinary'
  amount: number
  status: 'pending' | 'approved' | 'rejected' | 'issued'
  apply_time: string
}

export interface StatementListResponse {
  code: number
  message: string
  data: {
    list: Statement[]
    total: number
    page: number
    size: number
  }
}

export interface InvoiceListResponse {
  code: number
  message: string
  data: {
    list: Invoice[]
    total: number
    page: number
    size: number
  }
}

export const financeApi = {
  getStatementList(params: { page?: number; size?: number; month?: string }): Promise<StatementListResponse> {
    return request.get('/v1/pc/enterprise/finance/statement', { params })
  },
  getStatementDetail(statementId: string): Promise<{ code: number; message: string; data: any }> {
    return request.get('/v1/pc/enterprise/finance/statement/detail', { params: { statement_id: statementId } })
  },
  confirmStatement(statementId: string): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/finance/statement/confirm', { statement_id: statementId })
  },
  getInvoiceList(params: { page?: number; size?: number; status?: string }): Promise<InvoiceListResponse> {
    return request.get('/v1/pc/enterprise/finance/invoice', { params })
  },
  applyInvoice(data: { type: 'vat' | 'ordinary'; amount: number; reason: string }): Promise<{ code: number; message: string }> {
    return request.post('/v1/pc/enterprise/finance/invoice/apply', data)
  }
}

export default financeApi