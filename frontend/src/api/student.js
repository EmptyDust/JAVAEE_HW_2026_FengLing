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

// 更新学生自己的信息（学生自助）
export const updateMyProfile = (data) => {
  return request.put('/student/me', data)
}

// 获取学生详情
export const getStudentById = (id) => {
  return request.get(`/student/${id}`)
}

// 上传学生头像（两步流程：先上传文件到file-service，再更新头像记录）
export const uploadAvatar = async (file, studentId) => {
  // 第一步：上传文件到file-service
  const { uploadFile } = await import('./file')
  const uploadResult = await uploadFile(file, 'avatar', studentId)

  // 第二步：更新学生头像记录
  const params = {
    studentId,
    fileId: uploadResult.data.id,
    fileUrl: uploadResult.data.accessUrl
  }
  return request.post('/student/update-avatar', null, { params })
}
