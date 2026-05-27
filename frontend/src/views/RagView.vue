<template>
  <div class="rag-container">
    <el-row :gutter="20" style="height: 100%">
      <el-col :span="6">
        <el-card class="session-card">
          <template #header>
            <div class="session-header">
              <span>RAG会话</span>
              <el-button type="primary" size="small" @click="createNewSession">
                <el-icon><Plus /></el-icon> 新会话
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
              暂无RAG会话
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="chat-card">
          <template #header>
            <div class="card-header">
              <span>RAG知识库问答</span>
              <el-tag type="info" size="small">支持多轮追问</el-tag>
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
                  <div v-if="msg.context" class="message-context">
                    <el-divider content-position="left">引用资料</el-divider>
                    <div class="context-content">{{ msg.context }}</div>
                  </div>
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
                    正在检索知识库...
                  </div>
                </div>
              </div>
            </div>

            <div v-if="messages.length === 0" class="welcome-tip">
              <h3>RAG知识库问答</h3>
              <p>基于上传的文档进行智能问答，支持多轮追问</p>
              <div class="quick-commands">
                <el-divider content-position="left">快捷指令</el-divider>
                <div class="command-list">
                  <el-button
                    v-for="cmd in quickCommands"
                    :key="cmd.text"
                    size="small"
                    @click="useQuickCommand(cmd.prompt)"
                  >
                    {{ cmd.text }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div class="chat-input">
            <el-input
              v-model="inputQuestion"
              placeholder="请输入您的问题，AI将基于知识库回答... (Ctrl+Enter 发送)"
              :rows="3"
              type="textarea"
              @keyup.enter.ctrl="sendQuestion"
              :disabled="loading"
            />
            <el-button
              type="primary"
              :loading="loading"
              :disabled="!inputQuestion.trim()"
              @click="sendQuestion"
              class="send-btn"
            >
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="upload-card">
          <template #header>
            <span>文档上传</span>
          </template>

          <div class="upload-section">
            <input type="file" ref="fileInput" @change="handleFileChange"
                   accept=".pdf,.doc,.docx,.txt,.xls,.xlsx,.csv,.md,.html" style="display:none" />
            
            <el-form label-width="70px" size="small">
              <el-form-item label="分类">
                <el-select v-model="uploadForm.categoryId" placeholder="选择分类" clearable style="width:100%">
                  <el-option
                    v-for="cat in categories"
                    :key="cat.id"
                    :label="cat.categoryName"
                    :value="cat.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="标签">
                <el-input v-model="uploadForm.tags" placeholder="多个标签用逗号分隔" />
              </el-form-item>
            </el-form>

            <el-button type="primary" @click="triggerFileInput" :loading="uploading" style="width:100%">
              <el-icon><UploadFilled /></el-icon>
              选择文件上传
            </el-button>
            <div class="upload-tip">
              支持 PDF、Word、Excel、TXT、Markdown、HTML 格式，最大10MB
            </div>
          </div>

          <el-divider />

          <div class="document-list">
            <div class="doc-header">
              <h4>已上传文档</h4>
              <el-button size="small" @click="fetchDocuments">刷新</el-button>
            </div>
            <el-table :data="documentList" size="small" max-height="400">
              <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
              <el-table-column label="分类" width="80">
                <template #default="{ row }">
                  {{ getCategoryName(row.categoryId) }}
                </template>
              </el-table-column>
              <el-table-column label="标签" width="100" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.tags || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60">
                <template #default="{ row }">
                  <el-button size="small" type="danger" text @click="deleteDocument(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Loading, UploadFilled, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'
import axios from 'axios'

const sessions = ref([])
const messages = ref([])
const inputQuestion = ref('')
const loading = ref(false)
const uploading = ref(false)
const messagesRef = ref(null)
const fileInput = ref(null)
const documentList = ref([])
const categories = ref([])
const currentSessionId = ref(null)

const uploadForm = ref({
  categoryId: null,
  tags: ''
})

const quickCommands = [
  { text: '总结本文档', prompt: '请总结本文档的主要内容' },
  { text: '提取关键信息', prompt: '请提取本文档中的关键信息和要点' },
  { text: '生成摘要', prompt: '请为本文档生成一个简短的摘要' },
  { text: '回答相关问题', prompt: '基于本文档内容，回答以下问题：' },
  { text: '对比分析', prompt: '请对比分析文档中的不同观点或方案' }
]

const useQuickCommand = (prompt) => {
  inputQuestion.value = prompt
}

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
    const res = await api.get('/rag/sessions')
    sessions.value = res.data || []
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

const createNewSession = () => {
  currentSessionId.value = null
  messages.value = []
}

const switchSession = async (sessionId) => {
  currentSessionId.value = sessionId

  try {
    const res = await api.get(`/rag/sessions/${sessionId}/messages`)
    const records = res.data || []
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
        context: null,
        createTime: record.createTime
      })
    })
    scrollToBottom()
  } catch (error) {
    console.error('获取消息列表失败:', error)
  }
}

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定删除这个会话吗？', '提示')

    await api.delete(`/rag/sessions/${sessionId}`)
    ElMessage.success('删除成功')

    sessions.value = sessions.value.filter(s => s.id !== sessionId)

    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const fetchCategories = async () => {
  try {
    const res = await api.get('/category/list')
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.categoryName : '未分类'
}

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }

  uploading.value = true

  try {
    const formData = new FormData()
    formData.append('file', file)
    
    if (uploadForm.value.categoryId) {
      formData.append('categoryId', uploadForm.value.categoryId)
    }
    if (uploadForm.value.tags) {
      formData.append('tags', uploadForm.value.tags)
    }

    const token = localStorage.getItem('token')
    const response = await axios.post('/api/rag/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.data.code === 200) {
      ElMessage.success('文档上传成功')
      fetchDocuments()
      event.target.value = ''
      // 重置表单
      uploadForm.value = { categoryId: null, tags: '' }
    } else {
      ElMessage.error(response.data.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传错误:', error)
    ElMessage.error(error.response?.data?.msg || '文档上传失败')
  } finally {
    uploading.value = false
  }
}

const fetchDocuments = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/document/list', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.code === 200) {
      documentList.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取文档列表失败:', error)
  }
}

const deleteDocument = async (doc) => {
  try {
    await ElMessageBox.confirm(`确定删除文档 ${doc.fileName}？`, '提示')

    const token = localStorage.getItem('token')
    await axios.delete(`/api/document/delete?id=${doc.id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('删除成功')
    fetchDocuments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const sendQuestion = async () => {
  if (!inputQuestion.value.trim() || loading.value) return

  const question = inputQuestion.value.trim()
  messages.value.push({ role: 'user', content: question })
  inputQuestion.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const params = { question }
    if (currentSessionId.value) {
      params.sessionId = currentSessionId.value
    }

    const res = await api.get('/rag/chat', { params })
    const data = res.data

    messages.value.push({
      role: 'ai',
      content: data.answer,
      context: data.context
    })

    if (!currentSessionId.value && data.sessionId) {
      currentSessionId.value = data.sessionId
    }

    fetchSessions()
  } catch (error) {
    messages.value.push({ role: 'ai', content: '抱歉，请求失败，请稍后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  fetchSessions()
  fetchDocuments()
  fetchCategories()
})
</script>

<style scoped>
.rag-container {
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
  max-width: 80%;
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

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-context {
  margin-top: 12px;
}

.context-content {
  background-color: #ebeef5;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  max-height: 150px;
  overflow-y: auto;
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

.quick-commands {
  margin-top: 30px;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.command-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.chat-input {
  margin-top: 16px;
}

.send-btn {
  margin-top: 12px;
  width: 100%;
}

.upload-card {
  height: 100%;
}

.upload-section {
  text-align: center;
}

.upload-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.document-list {
  margin-top: 10px;
}

.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.doc-header h4 {
  margin: 0;
  color: #606266;
}
</style>
