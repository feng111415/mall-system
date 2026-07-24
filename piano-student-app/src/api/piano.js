import { request } from '../common/request'

const TEACHER_CODE_KEY = 'pianoTeacherCode'

function teacherHeader() {
  return {
    'X-Piano-Teacher-Code': uni.getStorageSync(TEACHER_CODE_KEY) || ''
  }
}

export function getTeacherCode() {
  return uni.getStorageSync(TEACHER_CODE_KEY) || ''
}

export function setTeacherCode(teacherCode) {
  uni.setStorageSync(TEACHER_CODE_KEY, teacherCode)
}

export function clearTeacherCode() {
  uni.removeStorageSync(TEACHER_CODE_KEY)
}

export function getHomework(submitCode) {
  return request({
    url: `/piano/portal/homework/${encodeURIComponent(submitCode)}`
  })
}

export function getSubmissionHistory(submitCode) {
  return request({
    url: `/piano/portal/history/${encodeURIComponent(submitCode)}`
  })
}

export function submitHomework(data) {
  return request({
    url: '/piano/portal/submission',
    method: 'POST',
    data
  })
}

export function loginTeacher(teacherCode) {
  return request({
    url: '/piano/teacher-portal/login',
    method: 'POST',
    data: { teacherCode }
  })
}

export function getTeacherStudents() {
  return request({
    url: '/piano/teacher-portal/students',
    header: teacherHeader()
  })
}

export function createTeacherStudent(data) {
  return request({
    url: '/piano/teacher-portal/students',
    method: 'POST',
    header: teacherHeader(),
    data
  })
}

export function updateTeacherStudent(studentId, data) {
  return request({
    url: `/piano/teacher-portal/students/${studentId}`,
    method: 'PUT',
    header: teacherHeader(),
    data
  })
}

export function deleteTeacherStudent(studentId) {
  return request({
    url: `/piano/teacher-portal/students/${studentId}`,
    method: 'DELETE',
    header: teacherHeader()
  })
}

export function getTeacherHomeworks(params = {}) {
  const query = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  return request({
    url: `/piano/teacher-portal/homeworks${query ? '?' + query : ''}`,
    header: teacherHeader()
  })
}

export function getTeacherHomeworkDetail(homeworkId) {
  return request({
    url: `/piano/teacher-portal/homeworks/${homeworkId}`,
    header: teacherHeader()
  })
}

export function createTeacherHomework(data) {
  return request({
    url: '/piano/teacher-portal/homeworks',
    method: 'POST',
    header: teacherHeader(),
    data
  })
}

export function updateTeacherHomework(homeworkId, data) {
  return request({
    url: `/piano/teacher-portal/homeworks/${homeworkId}`,
    method: 'PUT',
    header: teacherHeader(),
    data
  })
}

export function deleteTeacherHomework(homeworkId) {
  return request({
    url: `/piano/teacher-portal/homeworks/${homeworkId}`,
    method: 'DELETE',
    header: teacherHeader()
  })
}

export function getTeacherSubmissions(params = {}) {
  const query = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  return request({
    url: `/piano/teacher-portal/submissions${query ? '?' + query : ''}`,
    header: teacherHeader()
  })
}

export function getTeacherSubmissionDetail(submissionId) {
  return request({
    url: `/piano/teacher-portal/submissions/${submissionId}`,
    header: teacherHeader()
  })
}

export function reviewTeacherSubmission(data) {
  return request({
    url: '/piano/teacher-portal/submissions/review',
    method: 'PUT',
    header: teacherHeader(),
    data
  })
}
