<template>
  <div class="container">
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h2>消息通知</h2>
      <el-button link @click="readAll">全部已读</el-button>
    </div>
    <el-tabs v-model="type" @tab-change="load">
      <el-tab-pane label="全部" name="ALL" />
      <el-tab-pane label="赞和收藏" name="LIKE" />
      <el-tab-pane label="评论和回复" name="COMMENT" />
      <el-tab-pane label="关注" name="FOLLOW" />
      <el-tab-pane label="系统通知" name="SYSTEM" />
    </el-tabs>
    <el-empty v-if="filtered.length===0" description="暂无通知" />
    <div v-for="n in filtered" :key="n.id" class="item" :class="{unread: n.isRead===0}" @click="open(n)">
      <el-avatar :size="36">{{ (n.fromNickname || '系')[0] }}</el-avatar>
      <el-tag size="small" style="flex-shrink:0">{{ typeLabel(n.type) }}</el-tag>
      <b>{{ n.fromNickname || '系统通知' }}</b>
      <span class="content">{{ n.content }}</span>
      <span class="time">{{ n.createTime?.substring(0,16).replace('T',' ') }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const list = ref([])
const type = ref('ALL')
const load = async () => {
  const res = await api.get('/notification')
  list.value = res.data || []
}
const filtered = computed(() => {
  if (type.value === 'ALL') return list.value
  if (type.value === 'LIKE') return list.value.filter(n => n.type === 'LIKE' || n.type === 'FAVORITE')
  if (type.value === 'COMMENT') return list.value.filter(n => n.type === 'COMMENT')
  if (type.value === 'FOLLOW') return list.value.filter(n => n.type === 'FOLLOW')
  if (type.value === 'SYSTEM') return list.value.filter(n => n.type === 'SYSTEM')
  return list.value
})
const typeLabel = (t) => ({ LIKE: '赞', FAVORITE: '收藏', COMMENT: '评论', FOLLOW: '关注', SYSTEM: '系统' }[t] || t)
const readAll = async () => {
  await api.post('/notification/read-all')
  load()
}
const open = (n) => {
  if (n.articleId) router.push('/article/' + n.articleId)
}
onMounted(load)
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 8px; }
.item { padding: 12px; border-bottom: 1px solid #eee; display: flex; gap: 10px; cursor: pointer; align-items: center; }
.item.unread { background: #f0f7ff; }
.item .el-avatar { flex-shrink: 0; }
.content { flex: 1; }
.time { color: #aaa; font-size: 12px; white-space: nowrap; }
</style>
