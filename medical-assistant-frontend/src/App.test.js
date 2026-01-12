// 简单的组件测试示例
import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import App from './App.vue'

describe('App', () => {
  it('should render correctly', () => {
    const wrapper = mount(App)
    expect(wrapper.exists()).toBe(true)
  })
})