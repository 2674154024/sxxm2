import request from '@/api/request'

export interface ConversationItem {
  conversation_id: string
  target_user_id: string
  target_name: string
  target_avatar?: string
  last_message: string
  last_time: string
  unread_count: number
  is_online?: boolean
}

export function getConversations() {
  return request.get<any, { code: number; message: string; data: ConversationItem[] }>(
    '/v1/im/conversations'
  )
}

export interface MessageItem {
  message_id: string
  from_id: string
  to_id: string
  content: string
  message_type: string
  timestamp: number
  is_read: number
}

export function getMessages(params: {
  conversation_id: string
  page: number
  size: number
}) {
  return request.get<any, { code: number; message: string; data: { list: MessageItem[]; total: number } }>(
    '/v1/im/messages',
    { params }
  )
}

export function sendMessage(params: {
  target_id: string
  content: string
  message_type: string
}) {
  return request.post<any, { code: number; message: string; data: { message_id: string } }>(
    '/v1/student/im/send',
    params
  )
}
