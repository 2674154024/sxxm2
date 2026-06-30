import request from '../utils/request'

export interface Message {
  id: string
  conversation_id: string
  sender_id: string
  sender_name: string
  sender_avatar: string
  content: string
  message_type: 'text' | 'image' | 'file'
  file_url?: string
  file_name?: string
  created_at: string
  is_read: boolean
}

export interface Conversation {
  id: string
  peer_id: string
  peer_name: string
  peer_avatar: string
  last_message: string
  last_time: string
  unread_count: number
}

export interface SendMessageParams {
  conversation_id: string
  content: string
  message_type: 'text' | 'image' | 'file'
  file_url?: string
  file_name?: string
}

export interface GetMessagesParams {
  conversation_id: string
  page?: number
  size?: number
}

export interface MessageListResponse {
  code: number
  message: string
  data: {
    list: Message[]
    total: number
  }
}

export interface ConversationListResponse {
  code: number
  message: string
  data: {
    list: Conversation[]
    total: number
  }
}

export interface SendMessageResponse {
  code: number
  message: string
  data: Message
}

export const imApi = {
  getConversationList(): Promise<ConversationListResponse> {
    return request.get('/v1/student/im/conversations')
  },
  getMessages(params: GetMessagesParams): Promise<MessageListResponse> {
    return request.get('/v1/student/im/messages', { params })
  },
  sendMessage(data: SendMessageParams): Promise<SendMessageResponse> {
    return request.post('/v1/student/im/message', data)
  },
  getConversationDetail(conversationId: string): Promise<{
    code: number
    message: string
    data: Conversation
  }> {
    return request.get('/v1/student/im/conversation', { params: { conversation_id: conversationId } })
  }
}

export default imApi