import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter } from 'react-router'
import { AuthProvider } from '../auth/AuthContext.jsx'
import ProviderPortalPage from './ProviderPortalPage.jsx'

const api = vi.hoisted(() => ({
  getCurrentCustomer: vi.fn(), loginCustomer: vi.fn(), loginTechnician: vi.fn(), loginSupplier: vi.fn(),
  logoutCustomer: vi.fn(), registerCustomer: vi.fn(), getTechnicianProfile: vi.fn(), getSupplierProfile: vi.fn(),
  getTechnicianOffers: vi.fn(), getTechnicianAssignments: vi.fn(),
  acceptTechnicianOffer: vi.fn(), rejectTechnicianOffer: vi.fn(),
}))
vi.mock('../api.js', () => api)

function renderPage(role) {
  return render(<MemoryRouter><AuthProvider><ProviderPortalPage role={role} /></AuthProvider></MemoryRouter>)
}

describe('provider dashboards', () => {
  afterEach(() => { cleanup(); vi.clearAllMocks() })

  it('shows real technician capability fields and empty pending offers state', async () => {
    api.getCurrentCustomer.mockResolvedValue({ id: 4, fullName: 'Tech Ada', email: 'ada@example.com', role: 'TECHNICIAN' })
    api.getTechnicianProfile.mockResolvedValue({ displayName: 'Tech Ada', email: 'ada@example.com', active: true, serviceArea: 'North district', availability: 'NOT_CONFIGURED', capacityLimit: 3, serviceModes: ['REPAIR_CENTRE'], skills: [{ id: 2, name: 'Screen repair', categoryName: 'Phones' }] })
    api.getTechnicianOffers.mockResolvedValue([])
    api.getTechnicianAssignments.mockResolvedValue([])

    renderPage('TECHNICIAN')
    expect(await screen.findByText('North district')).toBeTruthy()
    expect(screen.getByText('Screen repair')).toBeTruthy()
    expect(await screen.findByText(/No pending assignment offers at this time/i)).toBeTruthy()
  })

  it('displays pending repair offers and allows accepting an offer', async () => {
    api.getCurrentCustomer.mockResolvedValue({ id: 4, fullName: 'Tech Ada', email: 'ada@example.com', role: 'TECHNICIAN' })
    api.getTechnicianProfile.mockResolvedValue({ displayName: 'Tech Ada', email: 'ada@example.com', active: true, serviceArea: 'North district', availability: 'AVAILABLE', capacityLimit: 3, serviceModes: ['HOME_VISIT'], skills: [{ id: 2, name: 'Screen repair', categoryName: 'Phones' }] })
    api.getTechnicianOffers.mockResolvedValue([
      {
        id: 101,
        bookingId: 55,
        service: { id: 1, name: 'Phone Screen Repair', categoryName: 'Phones' },
        gadget: { id: 10, brand: 'Apple', model: 'iPhone 13', categoryName: 'Phones' },
        serviceMode: 'HOME_VISIT',
        problemDescription: 'Cracked display on iPhone',
        matchScore: 95.0,
        matchRationale: 'Match Score: 95.0% · Skill: 100% (wt 50%), Availability: 100% (wt 30%), Remaining Capacity: 75% (wt 20%)',
        offerStatus: 'OFFERED',
        createdAt: '2026-10-26T12:00:00Z',
      },
    ])
    api.getTechnicianAssignments.mockResolvedValue([])
    api.acceptTechnicianOffer.mockResolvedValue({ id: 101, offerStatus: 'ACCEPTED' })

    renderPage('TECHNICIAN')
    expect(await screen.findByText('Phone Screen Repair')).toBeTruthy()
    expect(screen.getByText('Cracked display on iPhone')).toBeTruthy()
    expect(screen.getByText(/95.0% Match/i)).toBeTruthy()
    expect(screen.getByText(/Ranking & Match Breakdown/i)).toBeTruthy()
    expect(screen.getByText(/Remaining Capacity: 75%/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /Accept Assignment/i })).toBeTruthy()
    expect(screen.getByRole('button', { name: /Decline Offer/i })).toBeTruthy()
  })

  it('does not fabricate supplier inventory or order figures', async () => {
    api.getCurrentCustomer.mockResolvedValue({ id: 5, fullName: 'Supplier One', email: 's@example.com', role: 'SUPPLIER' })
    api.getSupplierProfile.mockResolvedValue({ businessName: 'Parts House', email: 's@example.com', contactEmail: null, active: true })
    renderPage('SUPPLIER')
    expect((await screen.findAllByText('Parts House')).length).toBe(2)
    expect(screen.getByText('Inventory is not enabled yet')).toBeTruthy()
    expect(screen.queryByText(/revenue|orders|stock on hand/i)).toBeNull()
  })
})
