import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Welcome from "@/views/Welcome";

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login,
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/welcome',
    name: 'Welcome',
    component: Welcome,
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})



export default router
