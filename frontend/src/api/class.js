import request from '../utils/request'

// 获取班级列表
export const getClassList = (params) => {
  return request.get('/class/list', { params })
}

// 获取所有班级（不分页，用于下拉选择）
export const getAllClasses = () => {
  return request.get('/class/all')
}

// 添加班级
export const addClass = (data) => {
  return request.post('/class/add', data)
}

// 更新班级
export const updateClass = (data) => {
  return request.put('/class/update', data)
}

// 删除班级
export const deleteClass = (id) => {
  return request.delete(`/class/delete/${id}`)
}
