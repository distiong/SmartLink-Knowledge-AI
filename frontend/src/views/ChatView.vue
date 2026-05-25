<template>
  <div class="chat-container">
    <el-row :gutter="20" style="height: 100%">
      <!-- 左侧会话列表 -->
      <el-col :span="6">
        <el-card class="session-card">
          <template #header>
            <div class="session-header">
              <span>对话列表</span>
              <el-button type="primary" size="small" @click="createNewSession">
                <el-icon><Plus /></el-icon> 新对话
              </el-button>
            </div>
          </template>
          
          <div class="session-list">
            <div
              v-for="session in sessions"
              :key="session.id"
              :class="['session-item', currentSessionId === session.id ? 'active' : '']"
              @click="switchSession(session.id)"
            >
              <div class="session-info">
                <div class="session-title">{{ session.title }}</div>
                <div class="session-meta">
                  <span>{{ session.messageCount }} 条消息</span>
                  <span>{{ formatTime(session.updateTime) }}</span>
                </div>
              </div>
              <el-button
                class="delete-btn"
                type="danger"
                size="small"
                text
                @click.stop="deleteSession(session.id)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            
            <div v-if="sessions.length === 0" class="empty-tip">
              暂无对话记录
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧对话区域 -->
      <el-col :span="18">
        <el-card class="chat-card">
          <template #header>
            <div class="card-header">
              <span>{{ currentSessionTitle }}</span>
              <el-tag type="info" size="small">
                消息长度限制: {{ maxMessageLength }} 字符
              </el-tag>
            </div>
          </template>
          
          <div class="chat-messages" ref="messagesRef">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']"
            >
              <div class="message-content">
                <div class="message-avatar">
                  <el-avatar :size="40">
                    {{ msg.role === 'user' ? '我' : 'AI' }}
                  </el-avatar>
                </div>
                <div class="message-text">
                  <div class="message-bubble">{{ msg.content }}</div>
                  <div class="message-time">{{ formatTime(msg.createTime) }}</div>
                </div>
              </div>
            </div>
            
            <div v-if="loading" class="message ai-message">
              <div class="message-content">
                <div class="message-avatar">
                  <el-avatar :size="40">AI</el-avatar>
                </div>
                <div class="message-text">
                  <div class="message-bubble loading-bubble">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    思考中...
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="messages.length === 0" class="welcome-tip">
              <h3>欢迎使用AI对话</h3>
              <p>请输入您的问题开始对话</p>
            </div>
          </div>
          
          <div class="chat-input">
            <div class="input-info">
              <span :class="{ 'text-danger': inputMessage.length > maxMessageLength }">
                {{ inputMessage.length }} / {{ maxMessageLength }}
              </span>
            </div>
            <el-input
              v-model="inputMessage"
              placeholder="请输入您的问题... (Ctrl+Enter 发送)"
              :rows="3"
              type="textarea"
              @keyup.enter.ctrl="sendMessage"
              :disabled="loading"
            />
            <el-button
              type="primary"
              :loading="loading"
              :disabled="!inputMessage.trim() || inputMessage.length > maxMessageLength"
              @click="sendMessage"
              class="send-btn"
            >
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Loading, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const maxMessageLength = 4000
const maxContextMessages = 20

const sessions = ref([])
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const currentSessionId = ref(null)
const currentSessionTitle = ref('新对话')

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const fetchSessions = async () => {
  try {
    const response = await axios.get('/api/ai/sessions', getHeaders())
    if (response.data.code === 200) {
      sessions.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

const createNewSession = async () => {
  try {
    const response = await axios.post('/api/ai/sessions', {}, getHeaders())
    if (response.data.code === 200) {
      const session = response.data.data
      sessions.value.unshift(session)
      switchSession(session.id)
    }
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

const switchSession = async (sessionId) => {
  currentSessionId.value = sessionId
  const session = sessions.value.find(s => s.id === sessionId)
  currentSessionTitle.value = session ? session.title : '新对话'
  
  try {
    const response = await axios.get(`/api/ai/sessions/${sessionId}/messages`, getHeaders())
    if (response.data.code === 200) {
      const records = response.data.data || []
      messages.value = []
      records.forEach(record => {
        messages.value.push({
          role: 'user',
          content: record.question,
          createTime: record.createTime
        })
        messages.value.push({
          role: 'ai',
          content: record.answer,
          createTime: record.createTime
        })
      })
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
  }
}

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定删除这个对话吗？', '提示')
    
    await axios.delete(`/api/ai/sessions/${sessionId}`, getHeaders())
    ElMessage.success('删除成功')
    
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    
    if (currentSessionId.value === sessionId) {
      if (sessions.value.length > 0) {
        switchSession(sessions.value[0].id)
      } else {
        currentSessionId.value = null
        messages.value = []
        currentSessionTitle.value = '新对话'
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  if (inputMessage.value.length > maxMessageLength) {
    ElMessage.error(`消息长度超过限制（最大${maxMessageLength}字符）`)
    return
  }
  
  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  messages.value.push({ role: 'user', content: userMessage })
  loading.value = true
  scrollToBottom()
  
  try {
    const response = await axios.post('/api/ai/chat', {
      message: userMessage,
      sessionId: currentSessionId.value
    }, getHeaders())
    
    if (response.data.code === 200) {
      const data = response.data.data
      messages.value.push({ role: 'ai', content: data.answer })
      
      // 更新sessionId
      if (!currentSessionId.value) {
        currentSessionId.value = data.sessionId
      }
      
      // 更新会话列表
      fetchSessions()
    } else {
      messages.value.push({ role: 'ai', content: '抱歉，请求失败：' + response.data.msg })
    }
  } catch (error) {
    messages.value.push({ role: 'ai', content: '抱歉，请求失败，请稍后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  fetchSessions()
})
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 140px);
}

.session-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 8px;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-title {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.message {
  margin-bottom: 20px;
}

.message-content {
  display: flex;
  gap: 12px;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.message-text {
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-break: break-word;
}

.user-message .message-bubble {
  background-color: #409eff;
  color: white;
}

.ai-message .message-bubble {
  background-color: #f4f4f5;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.user-message .message-time {
  text-align: right;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
}

.welcome-tip {
  text-align: center;
  color: #909399;
  padding: 100px 0;
}

.welcome-tip h3 {
  font-size: 20px;
  margin-bottom: 10px;
}

.chat-input {
  margin-top: 16px;
}

.input-info {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.text-danger {
  color: #f56c6c;
}

.send-btn {
  margin-top: 12px;
  width: 100%;
}
</style>
