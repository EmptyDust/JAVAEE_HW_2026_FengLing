import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userType = ref(localStorage.getItem('userType') || '')
  const studentId = ref(localStorage.getItem('studentId') || '')

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userId.value = info.userId
    username.value = info.username
    userType.value = info.userType || ''
    studentId.value = info.studentId || ''

    localStorage.setItem('userId', info.userId)
    localStorage.setItem('username', info.username)
    if (info.userType) {
      localStorage.setItem('userType', info.userType)
    }
    if (info.studentId) {
      localStorage.setItem('studentId', info.studentId)
    }
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    userType.value = ''
    studentId.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('userType')
    localStorage.removeItem('studentId')
  }

  return {
    token,
    userId,
    username,
    userType,
    studentId,
    setToken,
    setUserInfo,
    logout
  }
})
