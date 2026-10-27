import { act, cleanup, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router'
import { AuthProvider, useAuth } from './AuthContext.jsx'
import ProtectedRoute from '../components/ProtectedRoute.jsx'

const api = vi.hoisted(() => ({ getCurrentCustomer: vi.fn(), loginCustomer: vi.fn(), loginTechnician: vi.fn(), loginSupplier: vi.fn(), logoutCustomer: vi.fn(), registerCustomer: vi.fn() }))
vi.mock('../api.js', () => api)

function AccountStatus() {
  const { customer, status, login, logout } = useAuth()
  return <><p>{status}</p><p>{customer?.fullName || 'No signed-in customer'}</p><button onClick={() => login({ email: 'one@example.com', password: 'never-persist' })}>Test login</button><button onClick={() => login({ email: 'tech@example.com', password: 'never-persist' }, 'TECHNICIAN')}>Technician login</button><button onClick={logout}>Test logout</button></>
}

function LoginRoute() {
  const { identity, login, status } = useAuth()
  return <><p>Login route</p><p>{status}</p><button onClick={() => login({ email: 'tech@example.com', password: 'never-persist' }, 'TECHNICIAN')}>Technician login</button><p>{identity?.fullName || 'No signed-in identity'}</p></>
}

function renderGuard() {
  return render(<MemoryRouter initialEntries={['/account/gadgets']}><AuthProvider><Routes><Route path="/login" element={<LoginRoute />} /><Route element={<ProtectedRoute />}><Route path="/account/gadgets" element={<AccountStatus />} /></Route></Routes></AuthProvider></MemoryRouter>)
}

function renderProviderGuard(initialPath = '/supplier/dashboard') {
  return render(<MemoryRouter initialEntries={[initialPath]}><AuthProvider><Routes>
    <Route path="/supplier/login" element={<p>Supplier login route</p>} />
    <Route element={<ProtectedRoute requiredRole="SUPPLIER" />}><Route path="/supplier/dashboard" element={<p>Supplier dashboard</p>} /></Route>
    <Route element={<ProtectedRoute requiredRole="TECHNICIAN" />}><Route path="/technician/dashboard" element={<p>Technician dashboard</p>} /></Route>
  </Routes></AuthProvider></MemoryRouter>)
}

describe('customer session context', () => {
  afterEach(cleanup)
  beforeEach(() => {
    api.getCurrentCustomer.mockReset()
    api.loginCustomer.mockReset()
    api.loginTechnician.mockReset()
    api.loginSupplier.mockReset()
    api.logoutCustomer.mockReset()
  })

  it('uses the backend session as the source of truth and redirects unauthenticated routes', async () => {
    api.getCurrentCustomer.mockRejectedValueOnce(Object.assign(new Error('Unauthorized'), { status: 401 }))
    renderGuard()
    expect(await screen.findByText('Login route')).toBeTruthy()
  })

  it('initializes an authenticated customer and refreshes after login and logout', async () => {
    api.getCurrentCustomer.mockResolvedValueOnce({ id: 1, fullName: 'A Customer', email: 'one@example.com', role: 'CUSTOMER' })
    api.loginCustomer.mockResolvedValueOnce({ id: 1, fullName: 'A Customer', email: 'one@example.com', role: 'CUSTOMER' })
    api.logoutCustomer.mockResolvedValueOnce(null)
    renderGuard()
    expect(await screen.findByText('A Customer')).toBeTruthy()
    await act(async () => { screen.getByRole('button', { name: 'Test login' }).click() })
    expect(screen.getByText('authenticated')).toBeTruthy()
    await act(async () => { screen.getByRole('button', { name: 'Test logout' }).click() })
    expect(await screen.findByText('Login route')).toBeTruthy()
    expect(api.logoutCustomer).toHaveBeenCalledOnce()
  })

  it('uses the technician sign-in endpoint and accepts only a technician identity', async () => {
    api.getCurrentCustomer.mockRejectedValueOnce(Object.assign(new Error('Unauthorized'), { status: 401 }))
    api.loginTechnician.mockResolvedValueOnce({ id: 7, fullName: 'Technician One', email: 'tech@example.com', role: 'TECHNICIAN' })
    renderGuard()
    const button = await screen.findByRole('button', { name: 'Technician login' })
    await act(async () => { button.click() })
    expect(api.loginTechnician).toHaveBeenCalledOnce()
    expect(screen.getByText('authenticated')).toBeTruthy()
    expect(screen.getByText('Technician One')).toBeTruthy()
  })

  it('restores provider roles from the backend session and sends wrong-role visits to their own area', async () => {
    api.getCurrentCustomer.mockResolvedValueOnce({ id: 8, fullName: 'Technician One', email: 'tech@example.com', role: 'TECHNICIAN' })
    renderProviderGuard()
    expect(await screen.findByText('Technician dashboard')).toBeTruthy()
  })

  it('redirects unauthenticated suppliers to the supplier login page', async () => {
    api.getCurrentCustomer.mockRejectedValueOnce(Object.assign(new Error('Unauthorized'), { status: 401 }))
    renderProviderGuard()
    expect(await screen.findByText('Supplier login route')).toBeTruthy()
  })
})
