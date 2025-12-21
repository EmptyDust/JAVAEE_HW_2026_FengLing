import request from '../utils/request'

/**
 * 教师管理API
 */

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

// 上传教师头像
export const uploadTeacherAvatar = (teacherId, file) => {
  const formData = new FormData()
  formData.append('teacherId', teacherId)
  formData.append('file', file)
  return request.post('/teacher/avatar/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取教师的课程列表
export const getTeacherCourses = (teacherId) => {
  return request.get(`/teacher/courses/${teacherId}`)
}
