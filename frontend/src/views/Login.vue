<template>
  <div class="login-box">
    <h2>{{ isLogin ? '登录' : '注册' }}</h2>
    <el-input v-model="form.username" placeholder="用户名" style="margin-bottom:15px" />
    <el-input v-model="form.password" type="password" placeholder="密码" style="margin-bottom:15px" />
    <el-input v-if="!isLogin" v-model="form.nickname" placeholder="昵称" style="margin-bottom:15px" />
    <el-button type="primary" @click="submit" style="width:100%">{{ isLogin ? '登录' : '注册' }}</el-button>
    <p style="text-align:center;margin-top:15px">
      <el-link @click="isLogin = !isLogin">{{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}</el-link>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isLogin = ref(true)
const form = reactive({ username: '', password: '', nickname: '' })

const submit = async () => {
  try {
    if (isLogin.value) {
      const res = await api.post('/user/login', form)
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('nickname', res.data.nickname)
      localStorage.setItem('userId', res.data.userId)
      ElMessage.success('登录成功')
      window.location.href = '/'
    } else {
      await api.post('/user/register', form)
      ElMessage.success('注册成功，请登录')
      isLogin.value = true
      return
    }
  } catch (e) { ElMessage.error(e.response?.data?.message || '失败') }
}
</script>

<style scoped>
.login-box { max-width: 400px; margin: 80px auto; padding: 40px; background: #fff; border-radius: 8px; }
</style>
