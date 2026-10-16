<template>
  <div class="admin-layout">
    <div class="side-menu">
      <div class="menu-item" :class="{active: tab==='dashboard'}" @click="tab='dashboard'">
        <el-icon><Odometer /></el-icon> 仪表盘
      </div>
      <div class="menu-item" :class="{active: tab==='users'}" @click="tab='users'">
        <el-icon><User /></el-icon> 用户管理
      </div>
      <div class="menu-item" :class="{active: tab==='articles'}" @click="tab='articles'">
        <el-icon><Document /></el-icon> 文章管理
      </div>
      <div class="menu-item" :class="{active: tab==='comments'}" @click="tab='comments'">
        <el-icon><ChatDotRound /></el-icon> 评论管理
      </div>
      <div class="menu-item" :class="{active: tab==='reports'}" @click="tab='reports'">
        <el-icon><Warning /></el-icon> 举报处理
      </div>
    </div>
    <div class="main-content">
      <div v-if="tab==='dashboard'">
        <h2>仪表盘</h2>
        <div class="stats-row">
          <div class="stat-card"><div class="stat-num">{{ stats.userCount }}</div><div class="stat-label">总用户</div></div>
          <div class="stat-card"><div class="stat-num">{{ stats.articleCount }}</div><div class="stat-label">总文章</div></div>
          <div class="stat-card"><div class="stat-num">{{ stats.pendingReports }}</div><div class="stat-label">待处理举报</div></div>
          <div class="stat-card"><div class="stat-num">{{ stats.commentCount }}</div><div class="stat-label">总评论</div></div>
        </div>
        <div class="dashboard-grid">
          <div class="dash-card">
            <h4>近7天文章发布趋势</h4>
            <div ref="articleChart" class="chart-box"></div>
          </div>
          <div class="dash-card">
            <h4>用户增长趋势</h4>
            <div ref="userChart" class="chart-box"></div>
          </div>
          <div class="dash-card">
            <h4>分类分布</h4>
            <div ref="pieChart" class="chart-box"></div>
          </div>
          <div class="dash-card">
            <h4>最新举报 / 最新用户</h4>
            <div class="list-cols">
              <div>
                <div class="list-title">最新举报</div>
                <div v-for="r in latestReports" :key="r.id" class="report-row">
                  <span>#{{ r.articleId }}</span>
                  <span class="status-pending" v-if="r.status==='PENDING'">待处理</span>
                  <span class="status-done" v-else>已处理</span>
                </div>
              </div>
              <div>
                <div class="list-title">最新用户</div>
                <div v-for="u in latestUsers" :key="u.id" class="report-row">
                  <span>{{ u.nickname || u.username }}</span>
                  <span class="role-tag">{{ u.role }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="tab==='users'">
        <h2>用户管理</h2>
        <el-table :data="pagedUsers" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="role" label="角色" width="80" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">{{ row.status === 1 ? '正常' : '已封禁' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="banUser(row.id)">{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination :current-page="userPage" :page-size="pageSize" :total="users.length"
          layout="prev, pager, next" @current-change="userPage = $event" style="margin-top:15px" />
      </div>
      <div v-if="tab==='articles'">
        <h2>文章管理</h2>
        <el-table :data="pagedArticles" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">{{ row.status === 1 ? '正常' : '已下架' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" type="warning" @click="offline(row.id)">{{ row.status === 1 ? '下架' : '恢复' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination :current-page="artPage" :page-size="pageSize" :total="articles.length"
          layout="prev, pager, next" @current-change="artPage = $event" style="margin-top:15px" />
      </div>
      <div v-if="tab==='comments'">
        <h2>评论管理</h2>
        <el-table :data="pagedComments" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="articleId" label="文章ID" width="80" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="delComment(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination :current-page="commentPage" :page-size="pageSize" :total="comments.length"
          layout="prev, pager, next" @current-change="commentPage = $event" style="margin-top:15px" />
      </div>
      <div v-if="tab==='reports'">
        <h2>举报处理</h2>
        <el-table :data="pagedReports" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="类型" width="80">
            <template #default="{ row }">{{ row.targetType === 'ARTICLE' ? '文章' : '评论' }}</template>
          </el-table-column>
          <el-table-column label="目标ID" width="80">
            <template #default="{ row }">{{ row.targetType === 'ARTICLE' ? row.articleId : row.commentId }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="resolve(row.id)" v-if="row.status === 'PENDING'">标记已处理</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination :current-page="reportPage" :page-size="pageSize" :total="reports.length"
          layout="prev, pager, next" @current-change="reportPage = $event" style="margin-top:15px" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { Odometer, User, Document, ChatDotRound, Warning } from '@element-plus/icons-vue'
import api from '../api'

const tab = ref('dashboard')
const articles = ref([])
const reports = ref([])
const users = ref([])
const comments = ref([])
const artPage = ref(1)
const commentPage = ref(1)
const userPage = ref(1)
const reportPage = ref(1)
const pageSize = 10
const stats = ref({ userCount: 0, articleCount: 0, pendingReports: 0, commentCount: 0 })
const categoryDist = ref([])
const latestReports = ref([])
const latestUsers = ref([])
const weekTrend = ref([])
const userTrend = ref([])
const articleChart = ref(null)
const userChart = ref(null)
const pieChart = ref(null)
const pagedArticles = computed(() => articles.value.slice((artPage.value-1)*pageSize, artPage.value*pageSize))
const pagedComments = computed(() => comments.value.slice((commentPage.value-1)*pageSize, commentPage.value*pageSize))
const pagedUsers = computed(() => users.value.slice((userPage.value-1)*pageSize, userPage.value*pageSize))
const pagedReports = computed(() => reports.value.slice((reportPage.value-1)*pageSize, reportPage.value*pageSize))

const loadArticles = async () => { const res = await api.get('/admin/articles'); articles.value = res.data || [] }
const loadReports = async () => { const res = await api.get('/admin/reports'); reports.value = res.data || [] }
const loadUsers = async () => { try { const res = await api.get('/admin/users'); users.value = res.data || [] } catch(e){} }
const loadComments = async () => { try { const res = await api.get('/admin/comments'); comments.value = res.data || [] } catch(e){} }
const offline = async (id) => { await api.put('/admin/article/' + id + '/offline'); loadArticles() }
const resolve = async (id) => { await api.put('/admin/reports/' + id + '/resolve'); loadReports() }
const banUser = async (id) => { await api.put('/admin/user/' + id + '/ban'); loadUsers() }
const delComment = async (id) => { await api.delete('/admin/comment/' + id); loadComments() }

const loadCharts = async () => {
  try { const res = await api.get('/admin/stats'); stats.value = res.data || {} } catch(e){}
  try { const res = await api.get('/admin/category-dist'); categoryDist.value = res.data || [] } catch(e){}
  try { const res = await api.get('/admin/reports'); latestReports.value = (res.data || []).slice(0, 5) } catch(e){}
  try { const res = await api.get('/admin/users'); latestUsers.value = (res.data || []).slice(0, 5) } catch(e){}
  try { const res = await api.get('/admin/week-trend'); weekTrend.value = res.data || [] } catch(e){}
  try { const res = await api.get('/admin/user-trend'); userTrend.value = res.data || [] } catch(e){}
  setTimeout(() => renderCharts(), 200)
}

const renderCharts = () => {
  if (articleChart.value) {
    echarts.init(articleChart.value).setOption({
      tooltip: {},
      xAxis: { type: 'category', data: weekTrend.value.map(d => d.date) },
      yAxis: { type: 'value' },
      series: [{ data: weekTrend.value.map(d => d.count), type: 'line', smooth: true, areaStyle: {}, color: '#1e80ff' }]
    })
  }
  if (userChart.value) {
    echarts.init(userChart.value).setOption({
      tooltip: {},
      xAxis: { type: 'category', data: userTrend.value.map(d => d.date) },
      yAxis: { type: 'value' },
      series: [{ data: userTrend.value.map(d => d.count), type: 'line', smooth: true, areaStyle: {}, color: '#67c23a' }]
    })
  }
  if (pieChart.value) {
    echarts.init(pieChart.value).setOption({
      tooltip: {},
      series: [{
        type: 'pie', radius: '60%',
        data: categoryDist.value.map(c => ({ name: c.name, value: c.count }))
      }]
    })
  }
}

onMounted(() => { loadArticles(); loadReports(); loadUsers(); loadComments(); loadCharts() })
watch(tab, (v) => { if (v === 'dashboard') setTimeout(() => renderCharts(), 200) })
</script>

<style scoped>
.admin-layout { display: flex; gap: 20px; max-width: 1100px; margin: 20px auto; padding: 0 20px; }
.side-menu { width: 150px; flex-shrink: 0; background: #fff; border-radius: 8px; padding: 10px 0; height: fit-content; }
.menu-item { padding: 10px 16px; cursor: pointer; color: #555; display: flex; align-items: center; gap: 8px; }
.menu-item:hover { background: #f5f7fa; }
.menu-item.active { background: #e8f3ff; color: #1e80ff; }
.main-content { flex: 1; }
h2 { margin: 0 0 15px; }
.stats-row { display: flex; gap: 15px; margin-bottom: 20px; }
.stat-card { flex: 1; background: #fff; padding: 20px; border-radius: 8px; text-align: center; }
.stat-num { font-size: 28px; font-weight: bold; color: #1e80ff; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.dashboard-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.dash-card { background: #fff; padding: 20px; border-radius: 8px; }
.dash-card h4 { margin: 0 0 15px; }
.chart-box { height: 220px; }
.list-cols { display: flex; gap: 20px; }
.list-cols > div { flex: 1; }
.list-title { font-size: 13px; color: #666; margin-bottom: 8px; font-weight: 500; }
.report-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f0f0f0; font-size: 13px; }
.status-pending { color: #e6a23c; }
.status-done { color: #67c23a; }
.role-tag { font-size: 11px; color: #1e80ff; }
</style>
