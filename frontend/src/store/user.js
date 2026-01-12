import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userType = ref(localStorage.getItem('userType') || '')
  const studentId = ref(localStorage.getItem('studentId') || '')
  const teacherId = ref(localStorage.getItem('teacherId') || '')  // 新增

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userId.value = info.userId
    username.value = info.username
    userType.value = info.userType || ''
    studentId.value = info.studentId || ''
    teacherId.value = info.teacherId || ''  // 新增

    localStorage.setItem('userId', info.userId)
    localStorage.setItem('username', info.username)
    if (info.userType) {
      localStorage.setItem('userType', info.userType)
    }
    if (info.studentId) {
      localStorage.setItem('studentId', info.studentId)
    }
    if (info.teacherId) {
      localStorage.setItem('teacherId', info.teacherId)  // 新增
    }
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    userType.value = ''
    studentId.value = ''
    teacherId.value = ''  // 新增

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('userType')
    localStorage.removeItem('studentId')
    localStorage.removeItem('teacherId')  // 新增
  }

  return {
    token,
    userId,
    username,
    userType,
    studentId,
    teacherId,  // 新增
    setToken,
    setUserInfo,
    logout
  }
})
