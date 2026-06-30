export interface ComplaintTypeItem {
  key: string
  label: string
}

export interface ComplaintItem {
  complaint_id: string
  type: string
  type_text: string
  job_id?: string
  job_title?: string
  target_name: string
  content: string
  images: string[]
  status: number
  status_text: string
  create_time: string
}

export function getComplaintTypes() {
  return Promise.resolve({
    code: 200,
    message: 'success',
    data: [
      { key: 'fake_recruitment', label: '虚假招聘' },
      { key: 'salary_arrears', label: '薪资拖欠' },
      { key: 'deposit_fraud', label: '押金诈骗' },
      { key: 'unfulfilled', label: '未履约' },
      { key: 'info_leak', label: '信息泄露' },
      { key: 'other', label: '其他问题' },
    ] as ComplaintTypeItem[],
  })
}

export function createComplaint(params: {
  type: string
  job_id?: string
  target_name: string
  content: string
  images: string[]
}) {
  console.log('提交投诉:', params)
  return Promise.resolve({
    code: 200,
    message: '提交成功',
    data: {
      complaint_id: 'complaint_' + Date.now(),
    },
  })
}

export function getComplaintList(params: { page: number; size: number }) {
  console.log('获取投诉列表:', params)
  return Promise.resolve({
    code: 200,
    message: 'success',
    data: {
      list: [
        {
          complaint_id: 'c001',
          type: 'salary_arrears',
          type_text: '薪资拖欠',
          job_id: 'j001',
          job_title: '茶颜悦色门店店员',
          target_name: '茶颜悦色',
          content: '工作了一周，工资一直拖欠不发...',
          images: [],
          status: 2,
          status_text: '处理中',
          create_time: '2026-06-28 14:30',
        },
      ] as ComplaintItem[],
      total: 1,
    },
  })
}
