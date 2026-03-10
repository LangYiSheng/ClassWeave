<script setup>
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import { DAY_NAMES } from '@/config/app'

const props = defineProps({
  seriesGroups: {
    type: Array,
    default: () => [],
  },
})

const chartEl = ref(null)
let chartInstance
let resizeObserver

function renderChart() {
  if (!chartEl.value) {
    return
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartEl.value)
  }

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
    },
    grid: {
      left: 24,
      right: 16,
      top: 24,
      bottom: 24,
      containLabel: true,
    },
    legend: {
      bottom: 0,
      textStyle: {
        color: '#686d7f',
      },
    },
    xAxis: {
      type: 'category',
      data: DAY_NAMES,
      axisLine: {
        lineStyle: { color: 'rgba(31,34,48,0.12)' },
      },
      axisLabel: {
        color: '#686d7f',
      },
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false,
      },
      splitLine: {
        lineStyle: { color: 'rgba(31,34,48,0.08)' },
      },
      axisLabel: {
        color: '#686d7f',
      },
    },
    series: props.seriesGroups.map((group) => ({
      name: group.scheduleName,
      type: 'bar',
      barMaxWidth: 22,
      itemStyle: {
        color: group.color,
        borderRadius: [10, 10, 0, 0],
      },
      data: group.data.map((item) => item.totalPeriods),
    })),
  })
}

onMounted(() => {
  renderChart()
  resizeObserver = new ResizeObserver(() => {
    chartInstance?.resize()
  })
  resizeObserver.observe(chartEl.value)
})

watch(() => props.seriesGroups, renderChart, { deep: true })

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <div ref="chartEl" class="h-[320px] w-full"></div>
</template>
