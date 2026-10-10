<template>
  <div class="container">
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h2>搜索：{{ keyword }}</h2>
      <el-radio-group v-model="sort" size="small" @change="load">
        <el-radio-button value="new">最新</el-radio-button>
        <el-radio-button value="likes">最热</el-radio-button>
        <el-radio-button value="views">最多浏览</el-radio-button>
      </el-radio-group>
    </div>
    <el-empty v-if="list.length === 0" description="没有找到相关文章" />
    <ArticleCard v-for="a in list" :key="a.id" :article="a" />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'
import ArticleCard from '../components/ArticleCard.vue'

const route = useRoute()
const list = ref([])
const keyword = ref('')
const sort = ref('new')

const load = async () => {
  keyword.value = route.query.keyword || ''
  if (!keyword.value) { list.value = []; return }
  const res = await api.get('/article/search', { params: { keyword: keyword.value, page: 1, size: 20, sort: sort.value } })
  list.value = res.data.records || []
}
watch(() => route.query.keyword, load, { immediate: true })
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; padding: 0 20px; }
</style>
