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
@import './styles/variables.scss';
@import './styles/common.scss';

page {
  background-color: var(--color-gray-light);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: var(--color-text);
  font-size: var(--font-size-base);
  line-height: var(--font-line-height-base);
}

view, text {
  box-sizing: border-box;
}
</style>