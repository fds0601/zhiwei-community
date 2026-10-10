<template>
  <div class="container">
    <div class="main">
      <div class="filter-bar">
        <el-radio-group v-model="category" @change="onCategoryChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="RAG">RAG</el-radio-button>
          <el-radio-button label="Agent">Agent</el-radio-button>
          <el-radio-button label="LLM">LLM</el-radio-button>
          <el-radio-button label="教程">教程</el-radio-button>
        </el-radio-group>
      </div>
      <ArticleCard v-for="a in list" :key="a.id" :article="a" />
      <el-pagination :current-page="page" page-size="size"
        :total="total" layout="prev, pager, next" @current-change="handlePageChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import ArticleCard from '../components/ArticleCard.vue'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const category = ref('')

const loadList = async () => {
  const res = await api.get('/article/list', { params: { page: page.value, size: size.value, category: category.value } })
  list.value = res.data.records
  total.value = res.data.total
}
const handlePageChange = (p) => { page.value = p; loadList() }
const onCategoryChange = () => { page.value = 1; loadList() }
onMounted(loadList)
</script>

<style scoped>
.container { max-width: 1000px; margin: 20px auto; padding: 0 20px; }
.main { background: transparent; }
.filter-bar { margin-bottom: 15px; }
</style>
