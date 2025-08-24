import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import TransferList from '../views/TransferList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/transfers',
      name: 'transfers',
      component: TransferList
    }
  ]
})

export default router
