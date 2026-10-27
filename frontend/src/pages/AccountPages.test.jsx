import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router'
import LoginPage from './LoginPage.jsx'
import RegisterPage from './RegisterPage.jsx'
import GadgetsPage from './GadgetsPage.jsx'
import GadgetFormPage from './GadgetFormPage.jsx'

const api = vi.hoisted(() => ({
  getCatalogCategories: vi.fn(), getCustomerGadgets: vi.fn(), getCustomerGadget: vi.fn(),
  createCustomerGadget: vi.fn(), updateCustomerGadget: vi.fn(), deleteCustomerGadget: vi.fn(),
}))
const auth = vi.hoisted(() => ({ login: vi.fn(), register: vi.fn(), logout: vi.fn(), customer: null, status: 'unauthenticated' }))
vi.mock('../api.js', () => api)
vi.mock('../auth/AuthContext.jsx', () => ({ useAuth: () => auth }))

function renderPage(page, path = '/') {
  return render(<MemoryRouter initialEntries={[path]}><Routes><Route path="/" element={page} /><Route path="/account/gadgets/:id/edit" element={<GadgetFormPage />} /></Routes></MemoryRouter>)
}

describe('customer account pages', () => {
  afterEach(cleanup)
  beforeEach(() => {
    Object.values(api).forEach(mock => mock.mockReset())
    Object.values(auth).filter(value => typeof value === 'function').forEach(mock => mock.mockReset())
    auth.customer = null; auth.status = 'unauthenticated'
    api.getCatalogCategories.mockResolvedValue([{ id: 2, slug: 'smartphones', name: 'Smartphones' }])
  })

  it('validates registration confirmation before sending account data', async () => {
    renderPage(<RegisterPage />)
    fireEvent.change(screen.getByLabelText('Full name'), { target: { value: 'Ada Lovelace' } })
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'ada@example.com' } })
    fireEvent.change(document.querySelectorAll('input[type="password"]')[0], { target: { value: 'Correct-Password-123' } })
    fireEvent.change(screen.getByLabelText('Confirm password'), { target: { value: 'Not-the-same-123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Create account' }))
    expect((await screen.findByRole('alert')).textContent).toContain('Passwords do not match')
    expect(auth.register).not.toHaveBeenCalled()
  })

  it('shows invalid login feedback without persisting session data', async () => {
    auth.login.mockRejectedValueOnce(Object.assign(new Error('Email or password is incorrect.'), { status: 401 }))
    renderPage(<LoginPage />)
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'ada@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'wrong-password' } })
    fireEvent.click(screen.getByRole('button', { name: 'Log in' }))
    expect((await screen.findByRole('alert')).textContent).toContain('Email or password is incorrect')
  })

  it('shows an actionable empty gadget state', async () => {
    api.getCustomerGadgets.mockResolvedValueOnce([])
    renderPage(<GadgetsPage />)
    expect(await screen.findByText('No gadgets registered yet')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Register your first gadget' }).getAttribute('href')).toBe('/account/gadgets/new')
  })

  it('uses categories from the catalogue API and submits a gadget', async () => {
    api.createCustomerGadget.mockResolvedValueOnce({ id: 8 })
    renderPage(<GadgetFormPage />)
    await screen.findByRole('option', { name: 'Smartphones' })
    fireEvent.change(screen.getByLabelText('Device category'), { target: { value: '2' } })
    fireEvent.change(screen.getByLabelText('Brand / manufacturer'), { target: { value: 'Acme' } })
    fireEvent.change(screen.getByLabelText('Model'), { target: { value: 'Phone X' } })
    fireEvent.click(screen.getByRole('button', { name: 'Register gadget' }))
    await waitFor(() => expect(api.createCustomerGadget).toHaveBeenCalledWith({ categoryId: 2, brand: 'Acme', model: 'Phone X', purchaseDate: null, warrantyExpiryDate: null }))
  })
})
