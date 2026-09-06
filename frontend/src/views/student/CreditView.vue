<script setup>
import { onMounted, ref } from 'vue'
import { fetchMyBlacklistRecords, fetchMyBlacklistStatus } from '../../api/student'

const blacklistStatus = ref(false)
const records = ref([])

async function loadData() {
  const [statusResponse, recordsResponse] = await Promise.all([
    fetchMyBlacklistStatus(),
    fetchMyBlacklistRecords(),
  ])
  blacklistStatus.value = statusResponse.data
  records.value = recordsResponse.data
}

onMounted(loadData)
</script>

<template>
  <div class="page-grid">
    <section class="card summary-card">
      <div>
        <p class="eyebrow">信誉状态</p>
        <h2>我的信用</h2>
      </div>
      <span class="pill" :class="{ danger: blacklistStatus }">
        {{ blacklistStatus ? '封禁中' : '信誉正常' }}
      </span>
    </section>

    <section class="card">
      <div class="section-head">
        <div>
          <p class="eyebrow">违约记录</p>
          <h2>黑名单历史</h2>
        </div>
      </div>

      <div class="table-shell">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>原因</th>
              <th>时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.reason }}</td>
              <td>{{ item.violationDate }}</td>
              <td>
                <span class="pill">{{ item.status === 1 ? '封禁中' : '已解除' }}</span>
              </td>
            </tr>
            <tr v-if="records.length === 0">
              <td colspan="4" class="empty-cell">暂无黑名单记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
