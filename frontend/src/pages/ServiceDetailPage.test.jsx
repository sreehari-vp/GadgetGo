import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router'
import ServiceDetailPage from './ServiceDetailPage.jsx'

const api = vi.hoisted(() => ({ getCatalogServiceBySlug: vi.fn() }))
vi.mock('../api.js', () => ({
  ApiError: class ApiError extends Error { constructor(message, status) { super(message); this.status = status } },
  getCatalogServiceBySlug: api.getCatalogServiceBySlug,
}))
vi.mock('../auth/AuthContext.jsx', () => ({ useAuth: () => ({ customer: null, status: 'unauthenticated', logout: vi.fn() }) }))

function renderDetail() {
  return render(<MemoryRouter initialEntries={['/services/smartphone-screen-repair']}><Routes><Route path="/services/:slug" element={<ServiceDetailPage />} /></Routes></MemoryRouter>)
}

describe('ServiceDetailPage', () => {
  afterEach(cleanup)
  it('shows the service facts and assessment notice from the API', async () => {
    api.getCatalogServiceBySlug.mockResolvedValueOnce({
      slug: 'smartphone-screen-repair', name: 'Smartphone screen repair',
      shortDescription: 'Screen service description', detailedDescription: 'Assessment determines the repair.',
      category: { name: 'Smartphones', iconId: 'smartphones' }, repairTypeLabel: 'Screen repair',
    })
    renderDetail()
    expect(await screen.findByRole('heading', { name: 'Smartphone screen repair' })).toBeTruthy()
    expect(screen.getByText('Assessment determines the repair.')).toBeTruthy()
    expect(screen.getByText(/not a diagnosis, confirmed repair offer, booking, or price quote/i)).toBeTruthy()
  })

  it('reports an unavailable slug', async () => {
    api.getCatalogServiceBySlug.mockRejectedValueOnce(Object.assign(new Error('Missing service'), { status: 404 }))
    renderDetail()
    expect(await screen.findByText('This service is no longer available.')).toBeTruthy()
  })
})
