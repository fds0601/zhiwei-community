<template>
  <div class="container">
    <h2>管理后台</h2>
    <el-tabs v-model="tab">
      <el-tab-pane label="文章管理" name="articles">
        <el-table :data="articles" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">{{ row.status === 1 ? '正常' : '已下架' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" type="warning" @click="offline(row.id)">
                {{ row.status === 1 ? '下架' : '恢复' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="举报处理" name="reports">
        <el-table :data="reports" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="articleId" label="文章ID" width="80" />
          <el-table-column prop="reason" label="原因" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="resolve(row.id)" v-if="row.status === 'PENDING'">标记已处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const tab = ref('articles')
const articles = ref([])
const reports = ref([])

const loadArticles = async () => {
  const res = await api.get('/admin/articles')
  articles.value = res.data || []
}
const loadReports = async () => {
  const res = await api.get('/admin/reports')
  reports.value = res.data || []
}
const offline = async (id) => {
  await api.put('/admin/article/' + id + '/offline')
  loadArticles()
}
const resolve = async (id) => {
  await api.put('/admin/reports/' + id + '/resolve')
  loadReports()
}
onMounted(() => { loadArticles(); loadReports() })
</script>

<style scoped>
.container { max-width: 900px; margin: 20px auto; padding: 0 20px; }
</style>
