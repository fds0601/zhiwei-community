<template>
  <div class="container">
    <h2>热门标签</h2>
    <div class="tag-cloud">
      <el-tag v-for="t in tags" :key="t.tag" @click="$router.push('/search?keyword=' + encodeURIComponent(t.tag))"
        :size="Math.max(14, Math.min(24, 14 + t.count * 2))" class="cloud-tag">
        #{{ t.tag }} ({{ t.count }})
      </el-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
const tags = ref([])
onMounted(async () => {
  const res = await api.get('/tags/hot')
  tags.value = res.data || []
})
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
.tag-cloud { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 20px; }
.cloud-tag { cursor: pointer; padding: 8px 16px; }
</style>
