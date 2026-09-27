import { createRouter, createWebHistory } from 'vue-router'

import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ServerList from '../views/ServerList.vue'
import MyReservations from '../views/MyReservations.vue'
import AdminReservations from '../views/AdminReservations.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: Login
  },
  {
    path: '/register',
    component: Register
  },
  {
    path: '/servers',
    component: ServerList
  },
  {
    path: '/reservations/my',
    component: MyReservations
  },
  {
    path: '/admin/reservations',
    component: AdminReservations,
    meta:{
        requiresAdmin:true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token =
    localStorage.getItem('token')

  const user = JSON.parse(
    localStorage.getItem('user') || '{}'
  )

  const publicPaths = [
    '/login',
    '/register'
  ]

  if (
    !publicPaths.includes(to.path)
    &&
    !token
  ) {
    return '/login'
  }

  if (
    publicPaths.includes(to.path)
    &&
    token
  ) {
    return '/servers'
  }

  if (
    to.meta.requiresAdmin
    &&
    user.role !== 'ADMIN'
  ) {
    return '/servers'
  }
})

export default router