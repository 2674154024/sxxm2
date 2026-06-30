<script setup lang="ts">
import { ref, watch, computed } from 'vue'

export interface FilterData {
  salary_min?: number
  salary_max?: number
  settlement_type?: number
  distance?: number
  is_insured?: boolean
  work_time?: string
}

interface Props {
  visible: boolean
  defaultData?: FilterData
}

const props = withDefaults(defineProps<Props>(), {
  defaultData: () => ({}),
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', data: FilterData): void
  (e: 'reset'): void
}>()

const salaryOptions = [
  { label: '不限', min: undefined, max: undefined },
  { label: '15元以下', min: 0, max: 15 },
  { label: '15-20元', min: 15, max: 20 },
  { label: '20-30元', min: 20, max: 30 },
  { label: '30-50元', min: 30, max: 50 },
  { label: '50元以上', min: 50, max: undefined },
]

const settlementOptions = [
  { label: '不限', value: undefined },
  { label: '日结', value: 1 },
  { label: '周结', value: 2 },
  { label: '月结', value: 3 },
]

const distanceOptions = [
  { label: '不限', value: undefined },
  { label: '1km以内', value: 1 },
  { label: '3km以内', value: 3 },
  { label: '5km以内', value: 5 },
  { label: '10km以内', value: 10 },
]

const workTimeOptions = [
  { label: '不限', value: '' },
  { label: '上午', value: 'morning' },
  { label: '下午', value: 'afternoon' },
  { label: '晚班', value: 'evening' },
  { label: '周末', value: 'weekend' },
]

const selectedSalaryIndex = ref(0)
const selectedSettlementIndex = ref(0)
const selectedDistanceIndex = ref(0)
const selectedWorkTimeIndex = ref(0)
const isInsuredChecked = ref(false)

const hasSelection = computed(() => {
  return (
    selectedSalaryIndex.value !== 0 ||
    selectedSettlementIndex.value !== 0 ||
    selectedDistanceIndex.value !== 0 ||
    selectedWorkTimeIndex.value !== 0 ||
    isInsuredChecked.value
  )
})

function selectSalary(index: number) {
  selectedSalaryIndex.value = index
}

function selectSettlement(index: number) {
  selectedSettlementIndex.value = index
}

function selectDistance(index: number) {
  selectedDistanceIndex.value = index
}

function selectWorkTime(index: number) {
  selectedWorkTimeIndex.value = index
}

function toggleInsured() {
  isInsuredChecked.value = !isInsuredChecked.value
}

function handleConfirm() {
  const salary = salaryOptions[selectedSalaryIndex.value]
  const data: FilterData = {
    salary_min: salary.min,
    salary_max: salary.max,
    settlement_type: settlementOptions[selectedSettlementIndex.value].value,
    distance: distanceOptions[selectedDistanceIndex.value].value,
    is_insured: isInsuredChecked.value || undefined,
    work_time: workTimeOptions[selectedWorkTimeIndex.value].value || undefined,
  }
  emit('confirm', data)
  emit('update:visible', false)
}

function handleReset() {
  selectedSalaryIndex.value = 0
  selectedSettlementIndex.value = 0
  selectedDistanceIndex.value = 0
  selectedWorkTimeIndex.value = 0
  isInsuredChecked.value = false
  emit('reset')
}

function close() {
  emit('update:visible', false)
}

watch(
  () => props.visible,
  (val) => {
    if (val && props.defaultData) {
      const d = props.defaultData
      if (d.salary_min !== undefined || d.salary_max !== undefined) {
        const idx = salaryOptions.findIndex(
          (opt) => opt.min === d.salary_min && opt.max === d.salary_max
        )
        if (idx >= 0) selectedSalaryIndex.value = idx
      }
      if (d.settlement_type !== undefined) {
        const idx = settlementOptions.findIndex((opt) => opt.value === d.settlement_type)
        if (idx >= 0) selectedSettlementIndex.value = idx
      }
      if (d.distance !== undefined) {
        const idx = distanceOptions.findIndex((opt) => opt.value === d.distance)
        if (idx >= 0) selectedDistanceIndex.value = idx
      }
      if (d.work_time) {
        const idx = workTimeOptions.findIndex((opt) => opt.value === d.work_time)
        if (idx >= 0) selectedWorkTimeIndex.value = idx
      }
      isInsuredChecked.value = !!d.is_insured
    }
  }
)
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="filter-panel-overlay" @click="close">
      <div class="filter-panel" @click.stop>
        <div class="panel-header">
          <span class="panel-title">筛选</span>
          <span class="panel-close" @click="close">×</span>
        </div>

        <div class="panel-body">
          <div class="filter-section">
            <div class="section-title">薪资范围（元/时）</div>
            <div class="option-grid">
              <div
                v-for="(item, index) in salaryOptions"
                :key="index"
                class="option-item"
                :class="{ active: selectedSalaryIndex === index }"
                @click="selectSalary(index)"
              >
                {{ item.label }}
              </div>
            </div>
          </div>

          <div class="filter-section">
            <div class="section-title">结算方式</div>
            <div class="option-grid">
              <div
                v-for="(item, index) in settlementOptions"
                :key="index"
                class="option-item"
                :class="{ active: selectedSettlementIndex === index }"
                @click="selectSettlement(index)"
              >
                {{ item.label }}
              </div>
            </div>
          </div>

          <div class="filter-section">
            <div class="section-title">距离范围</div>
            <div class="option-grid">
              <div
                v-for="(item, index) in distanceOptions"
                :key="index"
                class="option-item"
                :class="{ active: selectedDistanceIndex === index }"
                @click="selectDistance(index)"
              >
                {{ item.label }}
              </div>
            </div>
          </div>

          <div class="filter-section">
            <div class="section-title">工作时间</div>
            <div class="option-grid">
              <div
                v-for="(item, index) in workTimeOptions"
                :key="index"
                class="option-item"
                :class="{ active: selectedWorkTimeIndex === index }"
                @click="selectWorkTime(index)"
              >
                {{ item.label }}
              </div>
            </div>
          </div>

          <div class="filter-section">
            <div class="section-title">其他筛选</div>
            <div class="checkbox-item" @click="toggleInsured">
              <span class="checkbox" :class="{ checked: isInsuredChecked }">
                <span v-if="isInsuredChecked">✓</span>
              </span>
              <span class="checkbox-label">仅看平台保障</span>
              <span class="checkbox-tip">薪资有保障</span>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <button class="btn-reset" @click="handleReset">重置</button>
          <button class="btn-confirm" @click="handleConfirm">
            确定
            <span v-if="hasSelection" class="confirm-count"></span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="scss">
.filter-panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  animation: overlayFadeIn 0.2s ease;
}

@keyframes overlayFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.filter-panel {
  width: 100%;
  max-height: 85vh;
  background-color: var(--color-bg);
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
  animation: panelSlideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes panelSlideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-base);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.panel-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text);
}

.panel-close {
  font-size: 24px;
  color: var(--color-text-secondary);
  cursor: pointer;
  line-height: 1;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: var(--spacing-base);
}

.filter-section {
  margin-bottom: var(--spacing-lg);

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.option-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.option-item {
  padding: 8px 16px;
  border-radius: 20px;
  background-color: var(--color-bg-secondary);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all 0.2s ease;

  &.active {
    background-color: var(--color-primary-light);
    color: var(--color-primary);
    border: 1px solid var(--color-primary);
    padding: 7px 15px;
  }

  &:active {
    transform: scale(0.96);
  }
}

.checkbox-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) 0;
  cursor: pointer;
}

.checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  margin-right: var(--spacing-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 12px;
  color: #fff;
  transition: all 0.2s ease;

  &.checked {
    background-color: var(--color-primary);
    border-color: var(--color-primary);
  }
}

.checkbox-label {
  font-size: var(--font-size-base);
  color: var(--color-text);
  flex: 1;
}

.checkbox-tip {
  font-size: 12px;
  color: var(--color-success);
  background-color: var(--color-success-bg);
  padding: 2px 6px;
  border-radius: 4px;
}

.panel-footer {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-base);
  padding-bottom: calc(var(--spacing-sm) + env(safe-area-inset-bottom));
  border-top: 1px solid var(--color-border);
  flex-shrink: 0;
}

.btn-reset {
  flex: 1;
  height: 48px;
  border: 1px solid var(--color-border);
  border-radius: 24px;
  background-color: var(--color-bg);
  color: var(--color-text);
  font-size: var(--font-size-lg);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    background-color: var(--color-bg-secondary);
  }
}

.btn-confirm {
  flex: 2;
  height: 48px;
  border: none;
  border-radius: 24px;
  background-color: var(--color-primary);
  color: #fff;
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}

.confirm-count {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--color-danger);
  margin-left: 4px;
  vertical-align: middle;
}
</style>
