<template>
  <div class="preview-container">
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>正在加载文档...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <el-icon :size="32" color="#f56c6c"><WarningFilled /></el-icon>
      <p>{{ error }}</p>
    </div>

    <div v-else class="content-container">
      <div v-if="fileType === 'pdf'" ref="pdfContainer" class="pdf-container"></div>

      <div v-else-if="fileType === 'docx' || fileType === 'doc'" class="word-container" v-html="wordContent"></div>

      <div v-else class="text-container">
        <pre>{{ textContent }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import * as pdfjsLib from 'pdfjs-dist'
import mammoth from 'mammoth'

pdfjsLib.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjsLib.version}/pdf.worker.min.js`

const props = defineProps({
  documentId: {
    type: Number,
    required: true
  },
  fileType: {
    type: String,
    required: true
  },
  content: {
    type: String,
    default: ''
  }
})

const loading = ref(true)
const error = ref(null)
const pdfContainer = ref(null)
const wordContent = ref('')
const textContent = ref('')

watch(() => props.documentId, () => {
  loadDocument()
})

onMounted(() => {
  loadDocument()
})

const loadDocument = async () => {
  loading.value = true
  error.value = null

  try {
    if (props.fileType === 'pdf') {
      await renderPdf()
    } else if (props.fileType === 'docx' || props.fileType === 'doc') {
      await renderWord()
    } else {
      textContent.value = props.content || '无法预览此文件类型'
    }
  } catch (e) {
    error.value = '文档加载失败: ' + e.message
  } finally {
    loading.value = false
  }
}

const renderPdf = async () => {
  if (!props.content) {
    error.value = 'PDF内容为空'
    return
  }

  try {
    const pdfDoc = await pdfjsLib.getDocument({ data: atob(props.content) }).promise
    const container = pdfContainer.value
    container.innerHTML = ''

    for (let i = 1; i <= pdfDoc.numPages; i++) {
      const page = await pdfDoc.getPage(i)
      const viewport = page.getViewport({ scale: 1.5 })

      const canvas = document.createElement('canvas')
      canvas.className = 'pdf-page'
      const context = canvas.getContext('2d')
      canvas.height = viewport.height
      canvas.width = viewport.width

      await page.render({
        canvasContext: context,
        viewport: viewport
      }).promise

      container.appendChild(canvas)
    }
  } catch (e) {
    error.value = 'PDF渲染失败: ' + e.message
  }
}

const renderWord = async () => {
  if (!props.content) {
    error.value = 'Word文档内容为空'
    return
  }

  try {
    const arrayBuffer = UintArray.from(atob(props.content), c => c.charCodeAt(0)).buffer
    const result = await mammoth.convertToHtml({ arrayBuffer })
    wordContent.value = result.value
  } catch (e) {
    wordContent.value = props.content
  }
}
</script>

<style scoped>
.preview-container {
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

.loading-container p,
.error-container p {
  margin-top: 12px;
}

.content-container {
  padding: 16px;
}

.pdf-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.pdf-page {
  max-width: 100%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.word-container {
  line-height: 1.6;
  font-size: 14px;
}

.word-container :deep(img) {
  max-width: 100%;
}

.text-container pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.6;
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
}
</style>
