import request from '../utils/request'

/**
 * 文件服务API
 */

// 上传文件到file-service
export const uploadFile = (file, category, relatedId) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', category)
  if (relatedId) {
    formData.append('businessId', relatedId)
  }
  return request.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export const deleteFile = (fileId) => {
  return request.delete(`/file/delete/${fileId}`)
}

// 下载文件
export const downloadFile = (fileId) => {
  return request.get(`/file/download/${fileId}`, {
    responseType: 'blob'
  })
}
