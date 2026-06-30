<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
page {
  background-color: #F2F3F5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

view, text {
  box-sizing: border-box;
}

.container {
  padding: 24rpx;
}

.flex {
  display: flex;
}

.flex-column {
  flex-direction: column;
}

.flex-center {
  align-items: center;
  justify-content: center;
}

.flex-between {
  justify-content: space-between;
}

.flex-1 {
  flex: 1;
}

.text-center {
  text-align: center;
}

.text-primary {
  color: #165DFF;
}

.text-gray {
  color: #86909C;
}

.text-dark {
  color: #1F2329;
}

.bg-white {
  background-color: #FFFFFF;
}

.mt-24 {
  margin-top: 24rpx;
}

.mb-24 {
  margin-bottom: 24rpx;
}

.ml-24 {
  margin-left: 24rpx;
}

.mr-24 {
  margin-right: 24rpx;
}

.p-24 {
  padding: 24rpx;
}
</style>