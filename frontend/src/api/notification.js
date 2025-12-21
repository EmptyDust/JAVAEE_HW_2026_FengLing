import request from '../utils/request'

/**
 * 通知管理API
 */

// 创建并发送通知
export const createNotification = (data) => {
  return request.post('/notification/create', data)
}

// 获取通知列表（分页）
export const getNotificationList = (params) => {
  return request.get('/notification/list', { params })
}

// 获取我的通知列表（分页）
export const getMyNotifications = (params) => {
  return request.get('/notification/my', { params })
}

// 标记通知为已读
export const markAsRead = (id) => {
  return request.put(`/notification/read/${id}`)
}

// 批量标记为已读
export const markAllAsRead = () => {
  return request.put('/notification/read/all')
}

// 获取未读通知数量
export const getUnreadCount = () => {
  return request.get('/notification/unread/count')
}

// 删除通知
export const deleteNotification = (id) => {
  return request.delete(`/notification/${id}`)
}

// 获取通知统计信息
export const getNotificationStatistics = (params) => {
  return request.get('/notification/statistics', { params })
}

/**
 * 通知模板API
 */

// 获取模板列表（分页）
export const getTemplateList = (params) => {
  return request.get('/notification/template/list', { params })
}

// 获取所有启用的模板
export const getAllTemplates = () => {
  return request.get('/notification/template/all')
}

// 根据类型获取模板
export const getTemplatesByType = (type) => {
  return request.get(`/notification/template/by-type/${type}`)
}

// 获取模板详情
export const getTemplate = (id) => {
  return request.get(`/notification/template/${id}`)
}

// 创建模板
export const createTemplate = (data) => {
  return request.post('/notification/template/create', data)
}

// 更新模板
export const updateTemplate = (data) => {
  return request.put('/notification/template/update', data)
}

// 删除模板
export const deleteTemplate = (id) => {
  return request.delete(`/notification/template/${id}`)
}

// 应用模板
export const applyTemplate = (id, variables) => {
  return request.post(`/notification/template/apply/${id}`, variables)
}

// 初始化默认模板
export const initDefaultTemplates = () => {
  return request.post('/notification/template/init-default')
}
