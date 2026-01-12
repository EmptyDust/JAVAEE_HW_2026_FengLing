import request from '../utils/request'

/**
 * 教师管理API
 */

// 获取教师自己的信息（教师自助）
export const getMyProfile = () => {
  return request.get('/teacher/me')
}

// 更新教师自己的信息（教师自助）
export const updateMyProfile = (data) => {
  return request.put('/teacher/me', data)
}

// 分页查询教师列表
export const getTeacherList = (params) => {
  return request.get('/teacher/list', { params })
}

// 获取所有教师列表（用于下拉选择）
export const getAllTeachers = () => {
  return request.get('/teacher/all')
}

// 获取教师详情
export const getTeacherById = (id) => {
  return request.get(`/teacher/${id}`)
}

// 添加教师
export const addTeacher = (data) => {
  return request.post('/teacher/add', data)
}

// 更新教师
export const updateTeacher = (data) => {
  return request.put('/teacher/update', data)
}

// 删除教师
export const deleteTeacher = (id) => {
  return request.delete(`/teacher/delete/${id}`)
}

// 上传教师头像（两步流程：先上传文件到file-service，再更新头像记录）
export const uploadTeacherAvatar = async (teacherId, file) => {
  // 第一步：上传文件到file-service
  const { uploadFile } = await import('./file')
  const uploadResult = await uploadFile(file, 'avatar', teacherId)

  // 第二步：更新教师头像记录
  const params = {
    teacherId,
    fileId: uploadResult.data.id
  }
  return request.post('/teacher/update-avatar', null, { params })
}

// 获取教师的课程列表
export const getTeacherCourses = (teacherId) => {
  return request.get(`/teacher/courses/${teacherId}`)
}
