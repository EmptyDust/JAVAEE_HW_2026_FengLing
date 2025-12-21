import request from '../utils/request'

export const getCaptcha = () => {
  return request.get('/auth/captcha')
}

export const register = (data) => {
  return request.post('/auth/register', data)
}

export const login = (data) => {
  return request.post('/auth/login', data)
}

export const logout = () => {
  return request.post('/auth/logout')
}

// 获取当前用户信息
export const getUserProfile = () => {
  return request.get('/user/profile')
}

// 更新用户信息
export const updateUserProfile = (data) => {
  return request.put('/user/profile', data)
}

// 修改密码
export const changePassword = (data) => {
  return request.put('/user/password', data)
}
