import { cleanup, render, screen, fireEvent, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router'
import { AuthProvider } from '../auth/AuthContext.jsx'
import BookingPage from './BookingPage.jsx'
import CustomerBookingsPage from './CustomerBookingsPage.jsx'

const api = vi.hoisted(() => ({
  getCurrentCustomer: vi.fn(),
  loginCustomer: vi.fn(),
  loginTechnician: vi.fn(),
  loginSupplier: vi.fn(),
  logoutCustomer: vi.fn(),
  registerCustomer: vi.fn(),
  getCatalogServiceBySlug: vi.fn(),
  getCustomerGadgets: vi.fn(),
  createBooking: vi.fn(),
  getCustomerBookings: vi.fn(),
}))
vi.mock('../api.js', () => api)

describe('Stage 5B Customer Booking Pages', () => {
  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('renders booking form with compatible gadgets and submits successfully', async () => {
    api.getCurrentCustomer.mockResolvedValue({
      id: 1,
      fullName: 'Alice Walker',
      email: 'alice@example.com',
      role: 'CUSTOMER',
    })

    api.getCatalogServiceBySlug.mockResolvedValue({
      id: 10,
      slug: 'screen-replacement',
      name: 'Screen Replacement',
      category: { id: 1, name: 'Phones', slug: 'phones' },
      supportedModes: ['HOME_VISIT', 'REPAIR_CENTRE'],
      priceStartingFrom: 49.99,
      durationEstimate: '1 hour',
    })

    api.getCustomerGadgets.mockResolvedValue([
      {
        id: 101,
        brand: 'Apple',
        model: 'iPhone 13',
        category: { id: 1, name: 'Phones', slug: 'phones' },
      },
      {
        id: 102,
        brand: 'Dell',
        model: 'XPS 15',
        category: { id: 2, name: 'Laptops', slug: 'laptops' },
      },
    ])

    api.createBooking.mockResolvedValue({
      id: 501,
      status: 'OFFERED',
      serviceMode: 'HOME_VISIT',
      problemDescription: 'Glass shattered on floor drop',
    })

    render(
      <MemoryRouter initialEntries={['/services/screen-replacement/book']}>
        <AuthProvider>
          <Routes>
            <Route path="/services/:slug/book" element={<BookingPage />} />
            <Route path="/account/bookings" element={<div>Bookings Dashboard</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    )

    // Verify service details loaded
    expect(await screen.findByText('Book Screen Replacement')).toBeTruthy()

    // Compatible gadget is in the select dropdown
    const select = screen.getByLabelText(/Select your device/i)
    expect(select).toBeTruthy()
    fireEvent.change(select, { target: { value: '101' } })

    // Fill problem description (at least 10 chars)
    const textarea = screen.getByLabelText(/Problem description/i)
    fireEvent.change(textarea, { target: { value: 'Screen glass completely shattered' } })

    // Submit repair booking
    const submitBtn = screen.getByRole('button', { name: /Submit repair booking/i })
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(api.createBooking).toHaveBeenCalledWith({
        gadgetId: 101,
        serviceId: 10,
        serviceMode: 'HOME_VISIT',
        problemDescription: 'Screen glass completely shattered',
      })
    })
  })

  it('renders customer bookings list with real statuses and badges', async () => {
    api.getCurrentCustomer.mockResolvedValue({
      id: 1,
      fullName: 'Alice Walker',
      email: 'alice@example.com',
      role: 'CUSTOMER',
    })

    api.getCustomerBookings.mockResolvedValue([
      {
        id: 501,
        status: 'ASSIGNED',
        serviceMode: 'HOME_VISIT',
        problemDescription: 'Cracked screen',
        createdAt: '2026-10-26T14:00:00Z',
        service: {
          id: 10,
          name: 'Screen Replacement',
          categoryName: 'Phones',
        },
        gadget: {
          id: 101,
          brand: 'Apple',
          model: 'iPhone 13',
          nickname: 'Alice Phone',
        },
        assignedTechnician: {
          id: 20,
          displayName: 'John Tech',
          serviceArea: 'Central',
        },
      },
      {
        id: 502,
        status: 'PENDING_MATCH',
        serviceMode: 'REPAIR_CENTRE',
        problemDescription: 'Battery drain issue',
        createdAt: '2026-10-26T15:00:00Z',
        service: {
          id: 11,
          name: 'Battery Service',
          categoryName: 'Phones',
        },
        gadget: {
          id: 101,
          brand: 'Apple',
          model: 'iPhone 13',
        },
        assignedTechnician: null,
      },
    ])

    render(
      <MemoryRouter>
        <AuthProvider>
          <CustomerBookingsPage />
        </AuthProvider>
      </MemoryRouter>
    )

    expect(await screen.findByText('Screen Replacement')).toBeTruthy()
    expect(screen.getByText('Battery Service')).toBeTruthy()
    expect(screen.getByText('Technician assigned')).toBeTruthy()
    expect(screen.getByText('Finding technician')).toBeTruthy()
    expect(screen.getByText('John Tech')).toBeTruthy()
  })

  it('shows empty state when customer has no bookings', async () => {
    api.getCurrentCustomer.mockResolvedValue({
      id: 1,
      fullName: 'Alice Walker',
      email: 'alice@example.com',
      role: 'CUSTOMER',
    })

    api.getCustomerBookings.mockResolvedValue([])

    render(
      <MemoryRouter>
        <AuthProvider>
          <CustomerBookingsPage />
        </AuthProvider>
      </MemoryRouter>
    )

    expect(await screen.findByText('No repair bookings yet')).toBeTruthy()
    expect(screen.getByRole('link', { name: /Browse repair services/i })).toBeTruthy()
  })
})
