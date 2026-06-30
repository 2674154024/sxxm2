import request from './request'

export interface EnterpriseInfo {
  enterprise_id: string
  enterprise_name: string
  is_certified: boolean
  credit_score: number
}

export interface JobItem {
  job_id: string
  job_title: string
  salary_amount: number
  work_address: string
  distance?: string
  industry_tag?: string
  settlement_type: number
  is_insured: boolean
  recruit_num: number
  current_num: number
  skill_require?: string
  job_desc: string
  enterprise_info: EnterpriseInfo
  create_time: string
}

export interface JobListParams {
  page?: number
  size?: number
  industry_tag?: string
  keyword?: string
  longitude?: number
  latitude?: number
  distance?: number
}

export interface JobListData {
  list: JobItem[]
  total: number
  page: number
  size: number
}

export function getJobList(params: JobListParams) {
  return request.get<any, { code: number; message: string; data: JobListData }>(
    '/v1/public/job/list',
    { params }
  )
}

export function getJobDetail(jobId: string) {
  return request.get<any, { code: number; message: string; data: JobItem }>(
    '/v1/public/job/detail',
    {
      params: { job_id: jobId },
    }
  )
}

export function getSimilarJobs(jobId: string, limit = 5) {
  return request.get<any, { code: number; message: string; data: JobItem[] }>(
    '/v1/public/job/similar',
    {
      params: { job_id: jobId, limit },
    }
  )
}

export function applyJob(jobId: string) {
  return request.post<any, { code: number; message: string; data: any }>(
    '/v1/student/apply',
    { job_id: jobId }
  )
}

export interface CategoryItem {
  key: string
  label: string
}

export function getCategories() {
  return Promise.resolve<{ code: number; message: string; data: CategoryItem[] }>({
    code: 200,
    message: 'success',
    data: [
      { key: 'all', label: '全部' },
      { key: 'tea', label: '茶饮' },
      { key: 'retail', label: '零售' },
      { key: 'tutor', label: '家教' },
      { key: 'exhibition', label: '会展' },
      { key: 'service', label: '上门服务' },
      { key: 'media', label: '新媒体' },
      { key: 'other', label: '其他' },
    ],
  })
}

export const mockJobList: JobItem[] = [
  {
    job_id: '1',
    job_title: '茶颜悦色门店店员',
    salary_amount: 18,
    work_address: '长沙市天心区黄兴南路步行商业街',
    distance: '2.3km',
    industry_tag: 'tea',
    settlement_type: 1,
    is_insured: true,
    recruit_num: 5,
    current_num: 2,
    skill_require: '无要求',
    job_desc: '负责门店点单、饮品制作、收银等工作。要求：18-30岁，男女不限，能吃苦耐劳，有服务行业经验者优先。',
    enterprise_info: {
      enterprise_id: 'e1',
      enterprise_name: '茶颜悦色(五一广场店)',
      is_certified: true,
      credit_score: 180,
    },
    create_time: '2024-01-15 10:30',
  },
  {
    job_id: '2',
    job_title: '星巴克咖啡师',
    salary_amount: 22,
    work_address: '长沙市芙蓉区IFS国金中心',
    distance: '1.5km',
    industry_tag: 'tea',
    settlement_type: 2,
    is_insured: true,
    recruit_num: 3,
    current_num: 1,
    skill_require: '英语基础',
    job_desc: '负责咖啡制作、顾客服务、门店清洁等工作。要求：形象气质佳，有良好的沟通能力，有咖啡经验者优先。',
    enterprise_info: {
      enterprise_id: 'e2',
      enterprise_name: '星巴克(IFS店)',
      is_certified: true,
      credit_score: 190,
    },
    create_time: '2024-01-14 14:20',
  },
  {
    job_id: '3',
    job_title: '优衣库店员',
    salary_amount: 19,
    work_address: '长沙市雨花区德思勤城市广场',
    distance: '3.8km',
    industry_tag: 'retail',
    settlement_type: 3,
    is_insured: false,
    recruit_num: 8,
    current_num: 3,
    skill_require: '无要求',
    job_desc: '负责卖场整理、顾客接待、收银等工作。要求：能吃苦耐劳，有团队协作精神。',
    enterprise_info: {
      enterprise_id: 'e3',
      enterprise_name: '优衣库(德思勤店)',
      is_certified: true,
      credit_score: 175,
    },
    create_time: '2024-01-13 09:15',
  },
  {
    job_id: '4',
    job_title: '家教老师（数学）',
    salary_amount: 80,
    work_address: '长沙市岳麓区银盆岭',
    distance: '5.2km',
    industry_tag: 'tutor',
    settlement_type: 1,
    is_insured: false,
    recruit_num: 5,
    current_num: 2,
    skill_require: '本科以上',
    job_desc: '负责初中/高中数学一对一辅导。要求：本科以上学历，数学相关专业，有家教经验者优先。',
    enterprise_info: {
      enterprise_id: 'e4',
      enterprise_name: '学而思教育',
      is_certified: true,
      credit_score: 185,
    },
    create_time: '2024-01-12 16:45',
  },
  {
    job_id: '5',
    job_title: '会展兼职礼仪',
    salary_amount: 200,
    work_address: '长沙国际会展中心',
    distance: '4.1km',
    industry_tag: 'exhibition',
    settlement_type: 1,
    is_insured: true,
    recruit_num: 20,
    current_num: 8,
    skill_require: '形象好',
    job_desc: '负责展会现场礼仪接待、引导等工作。要求：女生，身高165cm以上，形象气质佳。',
    enterprise_info: {
      enterprise_id: 'e5',
      enterprise_name: '湖南会展集团',
      is_certified: true,
      credit_score: 170,
    },
    create_time: '2024-01-11 11:00',
  },
]

export function getFavoriteList() {
  return Promise.resolve<{ code: number; message: string; data: { list: JobItem[]; total: number } }>({
    code: 200,
    message: 'success',
    data: {
      list: mockJobList.slice(0, 3),
      total: 3,
    },
  })
}

export function toggleFavorite(jobId: string) {
  console.log('切换收藏状态:', jobId)
  return Promise.resolve<{ code: number; message: string; data: { is_favorite: boolean } }>({
    code: 200,
    message: 'success',
    data: {
      is_favorite: true,
    },
  })
}
