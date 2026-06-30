<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getComplaintTypes, createComplaint, type ComplaintTypeItem } from '@/api/complaint'
import { getApplyList, type ApplyItem } from '@/api/apply'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const typeList = ref<ComplaintTypeItem[]>([])
const applyList = ref<ApplyItem[]>([])
const selectedType = ref('')
const selectedJobId = ref('')
const selectedJobTitle = ref('')
const targetName = ref('')
const content = ref('')
const images = ref<string[]>([])
const submitting = ref(false)
const showJobPicker = ref(false)

const contentLength = computed(() => content.value.length)
const canSubmit = computed(() => {
  return selectedType.value && content.value.length >= 20 && content.value.length <= 500 && !submitting.value
})

async function loadTypes() {
  try {
    const res = await getComplaintTypes()
    if (res.code === 200) {
      typeList.value = res.data
    }
  } catch (error) {
    console.error('加载投诉类型失败:', error)
  }
}

async function loadApplyList() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getApplyList({ page: 1, size: 20 })
    if (res.code === 200) {
      applyList.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载投递记录失败:', error)
  }
}

function selectType(item: ComplaintTypeItem) {
  selectedType.value = item.key
}

function selectJob(item: ApplyItem) {
  selectedJobId.value = item.job_id
  selectedJobTitle.value = item.job_title
  targetName.value = item.enterprise_name
  showJobPicker.value = false
}

function clearJob() {
  selectedJobId.value = ''
  selectedJobTitle.value = ''
}

function handleImageUpload() {
  if (images.value.length >= 9) {
    showToast({ message: '最多上传9张图片', type: 'fail' })
    return
  }
  const mockImage = `https://picsum.photos/200/200?random=${Date.now()}`
  images.value.push(mockImage)
}

function removeImage(index: number) {
  images.value.splice(index, 1)
}

async function handleSubmit() {
  if (!selectedType.value) {
    showToast({ message: '请选择投诉类型', type: 'fail' })
    return
  }
  if (content.value.length < 20) {
    showToast({ message: '投诉内容至少20字', type: 'fail' })
    return
  }

  try {
    submitting.value = true
    const res = await createComplaint({
      type: selectedType.value,
      job_id: selectedJobId.value || undefined,
      target_name: targetName.value,
      content: content.value,
      images: images.value,
    })

    if (res.code === 200) {
      showToast({ message: '投诉已提交，平台将在24小时内处理', type: 'success' })
      setTimeout(() => {
        router.back()
      }, 1500)
    }
  } catch (error: any) {
    showToast({ message: error?.message || '提交失败', type: 'fail' })
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadTypes()
  loadApplyList()
})
</script>

<template>
  <div class="complaint-create-page">
    <NavBar title="投诉举报" show-back @back="goBack" />

    <div class="form-container">
      <div class="form-card">
        <div class="form-item">
          <div class="form-label">
            <span class="required">*</span>
            投诉类型
          </div>
          <div class="type-list">
            <div
              v-for="item in typeList"
              :key="item.key"
              class="type-item"
              :class="{ active: selectedType === item.key }"
              @click="selectType(item)"
            >
              {{ item.label }}
            </div>
          </div>
        </div>
      </div>

      <div class="form-card">
        <div class="form-item">
          <div class="form-label">关联岗位（选填）</div>
          <div class="select-item" @click="showJobPicker = true">
            <span v-if="selectedJobTitle" class="selected-text">{{ selectedJobTitle }}</span>
            <span v-else class="placeholder">请选择关联的岗位</span>
            <span class="arrow">›</span>
          </div>
          <div v-if="selectedJobId" class="selected-job">
            <button class="clear-btn" @click.stop="clearJob">清除</button>
          </div>
        </div>

        <div class="form-item">
          <div class="form-label">被投诉方</div>
          <input
            v-model="targetName"
            type="text"
            class="text-input"
            placeholder="请输入企业名称或个人名称"
          />
        </div>
      </div>

      <div class="form-card">
        <div class="form-item">
          <div class="form-label">
            <span class="required">*</span>
            投诉内容（20-500字）
          </div>
          <textarea
            v-model="content"
            class="textarea"
            placeholder="请详细描述投诉内容，以便我们更快地为您处理..."
            maxlength="500"
          ></textarea>
          <div class="char-count" :class="{ warning: contentLength >= 480 }">
            {{ contentLength }}/500
          </div>
        </div>
      </div>

      <div class="form-card">
        <div class="form-item">
          <div class="form-label">证据上传（最多9张）</div>
          <div class="image-list">
            <div
              v-for="(img, index) in images"
              :key="index"
              class="image-item"
            >
              <img :src="img" class="uploaded-image" alt="证据图片" />
              <div class="remove-btn" @click="removeImage(index)">×</div>
            </div>
            <div
              v-if="images.length < 9"
              class="upload-btn"
              @click="handleImageUpload"
            >
              <span class="upload-icon">📷</span>
              <span class="upload-text">添加图片</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="submit-section">
      <button
        class="submit-btn"
        :class="{ active: canSubmit }"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        {{ submitting ? '提交中...' : '提交投诉' }}
      </button>
    </div>

    <div v-if="showJobPicker" class="picker-mask" @click="showJobPicker = false">
      <div class="picker-content" @click.stop>
        <div class="picker-header">
          <span class="picker-title">选择关联岗位</span>
          <span class="picker-close" @click="showJobPicker = false">×</span>
        </div>
        <div class="picker-list">
          <div
            v-for="item in applyList"
            :key="item.apply_id"
            class="picker-item"
            @click="selectJob(item)"
          >
            <div class="picker-item-title">{{ item.job_title }}</div>
            <div class="picker-item-sub">{{ item.enterprise_name }}</div>
          </div>
          <div v-if="applyList.length === 0" class="picker-empty">
            暂无投递记录
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.complaint-create-page {
  min-height: 100vh;
  background-color: var(--color-bg-secondary);
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}

.form-container {
  padding: var(--spacing-sm);
}

.form-card {
  background-color: var(--color-bg);
  border-radius: var(--radius-base);
  padding: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
}

.form-item {
  margin-bottom: var(--spacing-base);

  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);

  .required {
    color: var(--color-danger);
    margin-right: 2px;
  }
}

.type-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.type-item {
  padding: 8px 16px;
  border-radius: 20px;
  background-color: var(--color-bg-secondary);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all 0.2s ease;

  &.active {
    background-color: var(--color-primary);
    color: #fff;
  }

  &:active {
    transform: scale(0.96);
  }
}

.select-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 var(--spacing-sm);
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  cursor: pointer;

  &:active {
    background-color: var(--color-bg-tertiary);
  }
}

.selected-text {
  font-size: var(--font-size-base);
  color: var(--color-text);
}

.placeholder {
  font-size: var(--font-size-base);
  color: var(--color-text-disabled);
}

.arrow {
  font-size: 20px;
  color: var(--color-text-secondary);
}

.selected-job {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.clear-btn {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;

  &:active {
    color: var(--color-danger);
  }
}

.text-input {
  width: 100%;
  height: 44px;
  padding: 0 var(--spacing-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background-color: var(--color-bg);
  font-size: var(--font-size-base);
  color: var(--color-text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s ease;

  &:focus {
    border-color: var(--color-primary);
  }

  &::placeholder {
    color: var(--color-text-disabled);
  }
}

.textarea {
  width: 100%;
  min-height: 120px;
  padding: var(--spacing-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background-color: var(--color-bg);
  font-size: var(--font-size-base);
  color: var(--color-text);
  outline: none;
  resize: none;
  box-sizing: border-box;
  line-height: 1.6;
  transition: border-color 0.2s ease;

  &:focus {
    border-color: var(--color-primary);
  }

  &::placeholder {
    color: var(--color-text-disabled);
  }
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;

  &.warning {
    color: var(--color-warning);
  }
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.image-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 14px;
  line-height: 18px;
  text-align: center;
  cursor: pointer;
}

.upload-btn {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  background-color: var(--color-bg-secondary);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:active {
    background-color: var(--color-bg-tertiary);
  }
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-sm) var(--spacing-base);
  padding-bottom: calc(var(--spacing-sm) + env(safe-area-inset-bottom));
  background-color: var(--color-bg);
  border-top: 1px solid var(--color-border);
}

.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 24px;
  background-color: var(--color-text-disabled);
  color: #fff;
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: not-allowed;
  transition: all 0.2s ease;

  &.active {
    background-color: var(--color-danger);
    cursor: pointer;

    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }
}

.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  animation: maskFadeIn 0.2s ease;
}

@keyframes maskFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.picker-content {
  width: 100%;
  max-height: 70vh;
  background-color: var(--color-bg);
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
  animation: pickerSlideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes pickerSlideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-base);
  border-bottom: 1px solid var(--color-border);
}

.picker-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
}

.picker-close {
  font-size: 24px;
  color: var(--color-text-secondary);
  cursor: pointer;
  line-height: 1;
}

.picker-list {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.picker-item {
  padding: var(--spacing-base);
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;

  &:active {
    background-color: var(--color-bg-secondary);
  }
}

.picker-item-title {
  font-size: var(--font-size-base);
  color: var(--color-text);
  margin-bottom: 4px;
}

.picker-item-sub {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.picker-empty {
  padding: var(--spacing-xl);
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}
</style>
