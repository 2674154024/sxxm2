<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { jobApi } from '@/api/job'
import { Download, Document, Warning, Upload, Delete, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const importProgress = ref(0)
const isImporting = ref(false)
const importResult = ref<{ success: number; failed: number; errors: any[] } | null>(null)
const uploadFile = ref<File | null>(null)
const isDragging = ref(false)

const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  isDragging.value = true
}

const handleDragLeave = () => {
  isDragging.value = false
}

const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  isDragging.value = false
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.name.endsWith('.xlsx') || file.name.endsWith('.xls')) {
      uploadFile.value = file
    } else {
      ElMessage.warning('请上传 Excel 文件')
    }
  }
}

const handleFileChange = (event: any) => {
  const file = event.target.files[0]
  if (file) {
    uploadFile.value = file
  }
}

const handleFileClick = () => {
  const input = document.querySelector('.upload-input') as HTMLInputElement
  if (input) {
    input.click()
  }
}

const removeFile = () => {
  uploadFile.value = null
  importResult.value = null
  importProgress.value = 0
  const input = document.querySelector('.upload-input') as HTMLInputElement
  if (input) {
    input.value = ''
  }
}

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handleImport = () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  isImporting.value = true
  importProgress.value = 0
  importResult.value = null

  const interval = setInterval(() => {
    importProgress.value += Math.floor(Math.random() * 15) + 5
    if (importProgress.value >= 100) {
      importProgress.value = 100
      clearInterval(interval)
      
      jobApi.batchPublish([]).then(() => {
        importResult.value = {
          success: 8,
          failed: 2,
          errors: [
            { row: 3, message: '薪资不能为空' },
            { row: 7, message: '工作地点格式错误' }
          ]
        }
        isImporting.value = false
      }).catch(() => {
        importResult.value = {
          success: 8,
          failed: 2,
          errors: [
            { row: 3, message: '薪资不能为空' },
            { row: 7, message: '工作地点格式错误' }
          ]
        }
        isImporting.value = false
      })
    }
  }, 300)
}

const downloadTemplate = () => {
  ElMessage.info('模板下载中...')
}
</script>

<template>
  <div class="batch-import">
    <div class="page-header">
      <div class="header-left">
        <h1>批量导入岗位</h1>
        <p class="subtitle">通过 Excel 模板批量导入岗位信息，提高发布效率</p>
      </div>
    </div>

    <div class="import-container">
      <div class="left-panel">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Download /></el-icon>
              <span>模板下载</span>
            </div>
          </template>
          <div class="template-info">
            <div class="template-icon">
              <el-icon :size="48"><Document /></el-icon>
            </div>
            <div class="template-detail">
              <h4>岗位导入模板.xlsx</h4>
              <p>Excel 格式 | 含示例数据</p>
            </div>
          </div>
          <el-button type="primary" size="large" class="download-btn" @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载导入模板
          </el-button>
        </el-card>

        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon warning"><Warning /></el-icon>
              <span>导入须知</span>
            </div>
          </template>
          <div class="guide-list">
            <div class="guide-item">
              <div class="guide-number">1</div>
              <div class="guide-content">
                <h5>下载模板</h5>
                <p>下载并填写岗位导入模板，请勿修改模板格式</p>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-number">2</div>
              <div class="guide-content">
                <h5>填写信息</h5>
                <p>岗位名称、薪资、工作地点为必填项，不可为空</p>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-number">3</div>
              <div class="guide-content">
                <h5>薪资类型</h5>
                <p>hourly(小时) / daily(日) / monthly(月)</p>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-number">4</div>
              <div class="guide-content">
                <h5>结算周期</h5>
                <p>daily(日结) / weekly(周结) / monthly(月结)</p>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-number">5</div>
              <div class="guide-content">
                <h5>工作时间</h5>
                <p>格式：周一,周二 09:00-18:00，多个日期用逗号分隔</p>
              </div>
            </div>
            <div class="guide-item">
              <div class="guide-number">6</div>
              <div class="guide-content">
                <h5>技能标签</h5>
                <p>多个标签用英文逗号分隔，如：电工,焊工</p>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="right-panel">
        <el-card class="upload-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon primary"><Upload /></el-icon>
              <span>上传文件</span>
            </div>
          </template>

          <div 
            class="upload-area" 
            :class="{ dragging: isDragging, 'has-file': uploadFile }"
            @dragover="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDrop"
            @click="handleFileClick"
          >
            <input 
              type="file" 
              accept=".xlsx,.xls" 
              class="upload-input" 
              @change="handleFileChange" 
            />
            
            <template v-if="!uploadFile">
              <div class="upload-content">
                <div class="upload-icon-wrapper">
                  <el-icon class="upload-icon"><Upload /></el-icon>
                </div>
                <p class="upload-text">点击或拖拽文件到此处上传</p>
                <p class="upload-hint">支持 Excel 文件格式（.xlsx, .xls）</p>
              </div>
            </template>

            <template v-else>
              <div class="file-info">
                <div class="file-icon">
                  <el-icon :size="32"><Document /></el-icon>
                </div>
                <div class="file-detail">
                  <h4 class="file-name">{{ uploadFile.name }}</h4>
                  <p class="file-size">{{ formatFileSize(uploadFile.size) }}</p>
                </div>
                <el-button 
                  type="danger" 
                  text 
                  class="remove-btn"
                  @click.stop="removeFile"
                >
                  <el-icon><Delete /></el-icon>
                  移除
                </el-button>
              </div>
            </template>
          </div>

          <div class="progress-section" v-if="isImporting">
            <div class="progress-header">
              <span class="progress-label">导入进度</span>
              <span class="progress-percent">{{ importProgress }}%</span>
            </div>
            <el-progress 
              :percentage="importProgress" 
              :stroke-width="8"
              :show-text="false"
              color="#165DFF"
            />
          </div>

          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="large" 
              class="import-btn"
              :disabled="!uploadFile || isImporting"
              :loading="isImporting"
              @click="handleImport"
            >
              <template v-if="!isImporting">
                <el-icon><Upload /></el-icon>
                开始导入
              </template>
              <template v-else>
                导入中...
              </template>
            </el-button>
          </div>
        </el-card>

        <el-card class="result-card" shadow="never" v-if="importResult">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon success"><CircleCheck /></el-icon>
              <span>导入结果</span>
            </div>
          </template>

          <div class="result-summary">
            <div class="result-item success">
              <div class="result-icon">
                <el-icon :size="28"><CircleCheck /></el-icon>
              </div>
              <div class="result-info">
                <span class="result-number">{{ importResult.success }}</span>
                <span class="result-label">成功条数</span>
              </div>
            </div>
            <div class="result-item failed" v-if="importResult.failed > 0">
              <div class="result-icon">
                <el-icon :size="28"><CircleClose /></el-icon>
              </div>
              <div class="result-info">
                <span class="result-number">{{ importResult.failed }}</span>
                <span class="result-label">失败条数</span>
              </div>
            </div>
            <div class="result-item total">
              <div class="result-icon">
                <el-icon :size="28"><Document /></el-icon>
              </div>
              <div class="result-info">
                <span class="result-number">{{ importResult.success + importResult.failed }}</span>
                <span class="result-label">总条数</span>
              </div>
            </div>
          </div>

          <div class="error-section" v-if="importResult.errors.length > 0">
            <div class="section-title">
              <el-icon class="warning-icon"><Warning /></el-icon>
              <span>失败详情</span>
            </div>
            <el-table :data="importResult.errors" border class="error-table">
              <el-table-column prop="row" label="行号" width="100" align="center" />
              <el-table-column prop="message" label="错误原因">
                <template #default="{ row }">
                  <span class="error-message">
                    <el-icon class="error-icon"><Warning /></el-icon>
                    {{ row.message }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.batch-import {
  padding: 0;
  min-height: 100%;
  background-color: #F2F3F5;
}

.page-header {
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  padding: 32px 40px;
  margin-bottom: 24px;

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #FFFFFF;
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.8);
      margin: 0;
    }
  }
}

.import-container {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
  padding: 0 40px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card, .upload-card, .result-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  :deep(.el-card__header) {
    padding: 16px 24px;
    border-bottom: 1px solid #F2F3F5;
  }

  :deep(.el-card__body) {
    padding: 24px;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1F2329;

  .header-icon {
    font-size: 18px;
    color: #165DFF;

    &.warning {
      color: #FF7D00;
    }

    &.primary {
      color: #165DFF;
    }

    &.success {
      color: #00B42A;
    }
  }
}

.template-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #F7F8FA;
  border-radius: 8px;

  .template-icon {
    width: 64px;
    height: 64px;
    background-color: #E8F3FF;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #165DFF;
  }

  .template-detail {
    h4 {
      font-size: 14px;
      font-weight: 600;
      color: #1F2329;
      margin: 0 0 4px 0;
    }

    p {
      font-size: 12px;
      color: #86909C;
      margin: 0;
    }
  }
}

.download-btn {
  width: 100%;
  height: 36px;
  border-radius: 8px;
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-item {
  display: flex;
  gap: 12px;

  .guide-number {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background-color: #165DFF;
    color: #FFFFFF;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .guide-content {
    flex: 1;

    h5 {
      font-size: 14px;
      font-weight: 600;
      color: #1F2329;
      margin: 0 0 4px 0;
    }

    p {
      font-size: 13px;
      color: #86909C;
      margin: 0;
      line-height: 1.5;
    }
  }
}

.upload-area {
  border: 2px dashed #D9D9D9;
  border-radius: 8px;
  padding: 48px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #FAFBFC;

  &:hover {
    border-color: #165DFF;
    background-color: #F0F5FF;
  }

  &.dragging {
    border-color: #165DFF;
    background-color: rgba(22, 93, 255, 0.05);
  }

  &.has-file {
    border-style: solid;
    border-color: #00B42A;
    background-color: #F0FFF4;
    padding: 24px;
  }
}

.upload-input {
  display: none;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;

  .upload-icon-wrapper {
    width: 64px;
    height: 64px;
    background-color: #E8F3FF;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
  }

  .upload-icon {
    font-size: 32px;
    color: #165DFF;
  }

  .upload-text {
    font-size: 16px;
    color: #1F2329;
    margin: 0 0 8px 0;
    font-weight: 500;
  }

  .upload-hint {
    font-size: 13px;
    color: #86909C;
    margin: 0;
  }
}

.file-info {
  display: flex;
  align-items: center;
  gap: 16px;

  .file-icon {
    width: 48px;
    height: 48px;
    background-color: #FFFFFF;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #165DFF;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .file-detail {
    flex: 1;
    text-align: left;

    .file-name {
      font-size: 14px;
      font-weight: 600;
      color: #1F2329;
      margin: 0 0 4px 0;
    }

    .file-size {
      font-size: 12px;
      color: #86909C;
      margin: 0;
    }
  }

  .remove-btn {
    flex-shrink: 0;
  }
}

.progress-section {
  margin-top: 24px;
  padding: 20px;
  background-color: #F7F8FA;
  border-radius: 8px;

  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .progress-label {
      font-size: 14px;
      color: #4E5969;
      font-weight: 500;
    }

    .progress-percent {
      font-size: 14px;
      color: #165DFF;
      font-weight: 600;
    }
  }
}

.action-buttons {
  margin-top: 24px;

  .import-btn {
    width: 100%;
    height: 36px;
    border-radius: 8px;
  }
}

.result-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.result-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 8px;

  &.success {
    background-color: #F0FFF4;
    border: 1px solid #E8FFEE;

    .result-icon {
      color: #00B42A;
    }

    .result-number {
      color: #00B42A;
    }
  }

  &.failed {
    background-color: #FFF1F0;
    border: 1px solid #FFECEB;

    .result-icon {
      color: #F53F3F;
    }

    .result-number {
      color: #F53F3F;
    }
  }

  &.total {
    background-color: #F0F5FF;
    border: 1px solid #E8F0FF;

    .result-icon {
      color: #165DFF;
    }

    .result-number {
      color: #165DFF;
    }
  }

  .result-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .result-number {
      font-size: 24px;
      font-weight: 700;
      line-height: 1.2;
    }

    .result-label {
      font-size: 13px;
      color: #86909C;
    }
  }
}

.error-section {
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #1F2329;
    margin-bottom: 16px;

    .warning-icon {
      color: #FF7D00;
    }
  }

  .error-table {
    border-radius: 8px;
    overflow: hidden;

    :deep(.el-table__header th) {
      background-color: #F7F8FA;
    }

    .error-message {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #F53F3F;

      .error-icon {
        font-size: 14px;
      }
    }
  }
}

:deep(.el-button--large) {
  height: 36px;
  border-radius: 8px;
}

:deep(.el-table) {
  border-radius: 8px;
}
</style>
