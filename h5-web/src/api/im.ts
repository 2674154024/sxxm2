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
  return request.get<any, any>(
    '/v1/im/conversations'
  ).then((res: any) => {
    if (res.code === 200 && res.data) {
      res.data = res.data.map((item: any) => ({
        conversation_id: item.conversationId,
        target_user_id: item.targetId,
        target_name: item.targetName || '未知用户',
        target_avatar: item.targetAvatar || '',
        last_message: item.lastContent || '',
        last_time: item.lastTimestamp ? new Date(item.lastTimestamp).toISOString() : '',
        unread_count: item.unreadCount || 0,
        is_online: item.isOnline || false,
      }))
    }
    return res
  })
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
  target_id?: string
  page: number
  size: number
}) {
  return request.get<any, { code: number; message: string; data: { list: MessageItem[]; total: number } }>(
    '/v1/im/messages',
    { params: {
        conversation_id: params.conversation_id,
        targetId: params.target_id,
        target_id: params.target_id,
        page: params.page,
        size: params.size
      } }
  )
}

export function sendMessage(params: {
  target_id: string
  content: string
  message_type: string
}) {
  return request.post<any, { code: number; message: string; data: { message_id: string } }>(
    '/v1/student/im/send',
    {
      targetId: params.target_id,
      content: params.content,
      messageType: params.message_type,
    }
  )
}
