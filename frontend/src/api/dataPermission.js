import request from '../utils/request'

/**
 * 数据权限规则管理API
 */

// 分页查询权限规则列表
export function listDataPermissionRules(params) {
  return request({
    url: '/student/data-permission/list',
    method: 'get',
    params
  })
}

// 获取权限规则详情
export function getDataPermissionRule(id) {
  return request({
    url: `/student/data-permission/${id}`,
    method: 'get'
  })
}

// 添加权限规则
export function addDataPermissionRule(data) {
  return request({
    url: '/student/data-permission/add',
    method: 'post',
    data
  })
}

// 更新权限规则
export function updateDataPermissionRule(data) {
  return request({
    url: '/student/data-permission/update',
    method: 'put',
    data
  })
}

// 删除权限规则
export function deleteDataPermissionRule(id) {
  return request({
    url: `/student/data-permission/delete/${id}`,
    method: 'delete'
  })
}

// 启用/禁用权限规则
export function toggleDataPermissionRule(id) {
  return request({
    url: `/student/data-permission/toggle/${id}`,
    method: 'put'
  })
}

// 刷新权限规则缓存
export function refreshCache() {
  return request({
    url: '/student/data-permission/refresh-cache',
    method: 'post'
  })
}

// 获取所有角色列表
export function getRoles() {
  return request({
    url: '/student/data-permission/roles',
    method: 'get'
  })
}

// 获取所有表名列表
export function getTables() {
  return request({
    url: '/student/data-permission/tables',
    method: 'get'
  })
}
