import request from '../utils/request'

export const getStudentList = (params) => {
  return request.get('/student/list', { params })
}

export const addStudent = (data) => {
  return request.post('/student/add', data)
}

export const updateStudent = (data) => {
  return request.put('/student/update', data)
}

export const deleteStudent = (id) => {
  return request.delete(`/student/delete/${id}`)
}

export const getClassList = (params) => {
  return request.get('/class/list', { params })
}

export const getDictByType = (dictType) => {
  return request.get(`/dict/list/${dictType}`)
}

// 获取学生自己的信息
export const getMyProfile = () => {
  return request.get('/student/me')
}

// 获取学生详情
export const getStudentById = (id) => {
  return request.get(`/student/${id}`)
}

// 上传学生头像
export const uploadAvatar = (file, studentId) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('studentId', studentId)
  return request.post('/student/upload-avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
