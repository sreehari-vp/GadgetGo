const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '')
let csrfTokenPromise

export class ApiError extends Error {
  constructor(message, status, fields = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fields = fields
  }
}

export async function getCsrfToken({ refresh = false } = {}) {
  if (refresh) csrfTokenPromise = undefined
  if (!csrfTokenPromise) csrfTokenPromise = requestJson('/auth/csrf', { method: 'GET' }, { skipCsrf: true })
  return csrfTokenPromise
}

async function requestJson(path, options = {}, { skipCsrf = false } = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const headers = new Headers(options.headers || {})
  headers.set('Accept', 'application/json')
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (!skipCsrf && !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
    const csrf = await getCsrfToken()
    headers.set(csrf.headerName, csrf.token)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    method,
    headers,
    credentials: 'include',
  })
  if (!response.ok) {
    let message = `Request failed with HTTP ${response.status}.`
    let fields = {}
    try {
      const problem = await response.json()
      message = problem.detail || problem.title || message
      fields = problem.errors || {}
    } catch { /* Keep the useful HTTP fallback. */ }
    const error = new ApiError(message, response.status, fields)
    if (response.status === 403 && !skipCsrf) csrfTokenPromise = undefined
    if (response.status === 401 && path !== '/auth/login') {
      globalThis.window?.dispatchEvent(new Event('gadgetgo:unauthorized'))
    }
    throw error
  }
  if (response.status === 204) return null
  return response.json()
}

export async function getApplicationHealth(signal) {
  return requestJson('/health', { signal })
}

export function getCatalogCategories(signal) {
  return requestJson('/catalog/categories', { signal })
}

export function getCatalogRepairTypes(signal) {
  return requestJson('/catalog/repair-types', { signal })
}

export function getCatalogServices(filters = {}, signal) {
  const query = new URLSearchParams()
  for (const [key, value] of Object.entries(filters)) {
    if (value !== undefined && value !== null && value !== '') query.set(key, String(value))
  }
  const suffix = query.size ? `?${query}` : ''
  return requestJson(`/catalog/services${suffix}`, { signal })
}

export function getCatalogServiceBySlug(slug, signal) {
  return requestJson(`/catalog/services/${encodeURIComponent(slug)}`, { signal })
}

export async function registerCustomer(payload) {
  return requestJson('/auth/register', { method: 'POST', body: JSON.stringify(payload) })
}

async function loginAt(path, payload) {
  const identity = await requestJson(path, { method: 'POST', body: JSON.stringify(payload) })
  await getCsrfToken({ refresh: true })
  return identity
}

export function loginCustomer(payload) {
  return loginAt('/auth/login', payload)
}

export function loginTechnician(payload) {
  return loginAt('/auth/login/technician', payload)
}

export function loginSupplier(payload) {
  return loginAt('/auth/login/supplier', payload)
}

export function getCurrentCustomer(signal) {
  return requestJson('/auth/me', { signal })
}

export async function logoutCustomer() {
  await requestJson('/auth/logout', { method: 'POST' })
  await getCsrfToken({ refresh: true })
}

export function getCustomerProfile(signal) {
  return requestJson('/customers/me', { signal })
}

export function updateCustomerProfile(payload) {
  return requestJson('/customers/me', { method: 'PUT', body: JSON.stringify(payload) })
}

export function getCustomerGadgets(signal) {
  return requestJson('/gadgets', { signal })
}

export function getCustomerGadget(id, signal) {
  return requestJson(`/gadgets/${encodeURIComponent(id)}`, { signal })
}

export function createCustomerGadget(payload) {
  return requestJson('/gadgets', { method: 'POST', body: JSON.stringify(payload) })
}

export function updateCustomerGadget(id, payload) {
  return requestJson(`/gadgets/${encodeURIComponent(id)}`, { method: 'PUT', body: JSON.stringify(payload) })
}

export function deleteCustomerGadget(id) {
  return requestJson(`/gadgets/${encodeURIComponent(id)}`, { method: 'DELETE' })
}

export function getTechnicianProfile(signal) {
  return requestJson('/technician/me', { signal })
}

export function getTechnicianSkills(signal) {
  return requestJson('/technician/skills', { signal })
}

export function getSupplierProfile(signal) {
  return requestJson('/supplier/me', { signal })
}

export function createBooking(payload) {
  return requestJson('/bookings', { method: 'POST', body: JSON.stringify(payload) })
}

export function getCustomerBookings(signal) {
  return requestJson('/bookings', { signal })
}

export function getCustomerBooking(id, signal) {
  return requestJson(`/bookings/${encodeURIComponent(id)}`, { signal })
}

export function getTechnicianOffers(signal) {
  return requestJson('/technician/assignments/offers', { signal })
}

export function acceptTechnicianOffer(offerId) {
  return requestJson(`/technician/assignments/offers/${encodeURIComponent(offerId)}/accept`, { method: 'POST' })
}

export function rejectTechnicianOffer(offerId) {
  return requestJson(`/technician/assignments/offers/${encodeURIComponent(offerId)}/reject`, { method: 'POST' })
}

export function getTechnicianAssignments(signal) {
  return requestJson('/technician/assignments', { signal })
}

export function rematchBooking(bookingId) {
  return requestJson(`/bookings/${encodeURIComponent(bookingId)}/rematch`, { method: 'POST' })
}
