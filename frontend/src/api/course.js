import request from '../utils/request'

/**
 * 课程管理API
 */

// ========== 课程信息 ==========

// 获取课程列表
export const getCourseList = (params) => {
  return request.get('/course/list', { params })
}

// 获取课程详情
export const getCourseById = (id) => {
  return request.get(`/course/${id}`)
}

// 创建课程
export const createCourse = (data) => {
  return request.post('/course/add', data)
}

// 更新课程
export const updateCourse = (data) => {
  return request.put('/course/update', data)
}

// 删除课程
export const deleteCourse = (id) => {
  return request.delete(`/course/delete/${id}`)
}

// ========== 课程附件 ==========

// 获取课程附件列表
export const getCourseAttachments = (courseId) => {
  return request.get(`/course/attachment/list/${courseId}`)
}

// 上传课程附件（两步流程：先上传文件到file-service，再创建附件记录）
export const uploadAttachment = async (file, courseId, description) => {
  // 第一步：上传文件到file-service
  const { uploadFile } = await import('./file')
  const uploadResult = await uploadFile(file, 'course-attachment', courseId)

  // 第二步：创建附件记录
  const params = {
    courseId,
    fileId: uploadResult.data.id,
    fileName: file.name,
    filePath: uploadResult.data.filePath,
    fileSize: file.size,
    mimeType: file.type,
    description: description || ''
  }
  return request.post('/course/attachment/create', null, { params })
}

// 删除课程附件
export const deleteAttachment = (id) => {
  return request.delete(`/course/attachment/delete/${id}`)
}

// 获取所有附件和统计信息（附件管理界面专用）
export const getAllAttachmentsWithStatistics = (params) => {
  return request.get('/course/attachment/all', { params })
}

// 记录附件下载
export const recordDownload = (id) => {
  return request.post(`/course/attachment/download/${id}`)
}

// 记录附件浏览
export const recordView = (id) => {
  return request.post(`/course/attachment/view/${id}`)
}

// ========== 学生选课 ==========

// 学生选课
export const enrollCourse = (courseId, studentNumber, classId, className) => {
  const params = { courseId }
  if (studentNumber) params.studentNumber = studentNumber
  if (classId) params.classId = classId
  if (className) params.className = className
  return request.post('/course/enrollment/enroll', null, { params })
}

// 学生退课
export const dropCourse = (enrollmentId) => {
  return request.post(`/course/enrollment/drop/${enrollmentId}`)
}

// 查询我的选课
export const getMyEnrollments = (params) => {
  return request.get('/course/enrollment/my', { params })
}

// 查询课程的选课学生
export const getCourseStudents = (courseId, params) => {
  return request.get(`/course/enrollment/students/${courseId}`, { params })
}

// 检查是否已选课
export const checkEnrollment = (courseId) => {
  return request.get('/course/enrollment/check', { params: { courseId } })
}

// 录入成绩
export const updateScore = (enrollmentId, score, grade) => {
  const params = { enrollmentId, score }
  if (grade) params.grade = grade
  return request.post('/course/enrollment/score', null, { params })
}

// ========== 课程日历 ==========

// 获取课程日历
export const getCourseCalendar = (courseId) => {
  return request.get(`/course/calendar/${courseId}`)
}

// 获取指定日期范围的课程日历
export const getCalendarByDateRange = (courseId, startDate, endDate) => {
  return request.get(`/course/calendar/${courseId}/range`, {
    params: { startDate, endDate }
  })
}

// 获取学生所有课程日历
export const getStudentCalendar = (courseIds, startDate, endDate) => {
  return request.post('/course/calendar/student', courseIds, {
    params: { startDate, endDate }
  })
}

// 获取学生所有课程日历（按学期）
export const getStudentCalendarBySemester = (courseIds, semester) => {
  return request.post('/course/calendar/student/semester', courseIds, {
    params: { semester }
  })
}

// 获取可用学期列表
export const getAvailableSemesters = () => {
  return request.get('/course/calendar/semesters')
}

// 创建日历事件
export const createCalendarEvent = (data) => {
  return request.post('/course/calendar/add', data)
}

// 更新日历事件
export const updateCalendarEvent = (data) => {
  return request.put('/course/calendar/update', data)
}

// 删除日历事件
export const deleteCalendarEvent = (id) => {
  return request.delete(`/course/calendar/delete/${id}`)
}

// 取消日历事件
export const cancelCalendarEvent = (id) => {
  return request.post(`/course/calendar/cancel/${id}`)
}

// 获取课程的原始日历记录（不展开周期性事件）
export const getRawCourseCalendar = (courseId) => {
  return request.get(`/course/calendar/raw/${courseId}`)
}

// 批量生成课程日历
export const generateCourseCalendar = (courseId, startDate, endDate) => {
  return request.post(`/course/calendar/generate/${courseId}`, null, {
    params: { startDate, endDate }
  })
}

// ========== 附件搜索 ==========

// 全文搜索附件
export const searchAttachmentsByKeyword = (keyword, page = 0, size = 10) => {
  return request.get('/course/attachment/search/keyword', {
    params: { keyword, page, size }
  })
}

// 按课程搜索附件
export const searchAttachmentsByCourse = (courseId) => {
  return request.get(`/course/attachment/search/course/${courseId}`)
}

// 高级搜索附件
export const advancedSearchAttachments = (keyword, courseId, fileType, page = 0, size = 10) => {
  return request.get('/course/attachment/search/advanced', {
    params: { keyword, courseId, fileType, page, size }
  })
}
