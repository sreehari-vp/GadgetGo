import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import CataloguePage from './CataloguePage.jsx'

const api = vi.hoisted(() => ({
  getCatalogCategories: vi.fn(),
  getCatalogRepairTypes: vi.fn(),
  getCatalogServices: vi.fn(),
}))
vi.mock('../api.js', () => api)
vi.mock('../auth/AuthContext.jsx', () => ({ useAuth: () => ({ customer: null, status: 'unauthenticated', logout: vi.fn() }) }))

const categories = [{ id: 1, slug: 'smartphones', name: 'Smartphones', iconId: 'phone' }]
const types = [{ slug: 'screen-repair', label: 'Screen repair' }]
const page = { content: [], page: 0, size: 9, totalElements: 0, totalPages: 0, first: true, last: true }

function renderCatalogue(initialEntry = '/services') {
  return render(<MemoryRouter initialEntries={[initialEntry]}><CataloguePage /></MemoryRouter>)
}

describe('CataloguePage', () => {
  afterEach(cleanup)
  beforeEach(() => {
    api.getCatalogCategories.mockResolvedValue(categories)
    api.getCatalogRepairTypes.mockResolvedValue(types)
    api.getCatalogServices.mockResolvedValue(page)
  })

  it('loads data-driven filters and shows the empty state', async () => {
    renderCatalogue()
    expect(screen.getByText('Loading services…')).toBeTruthy()
    expect(await screen.findByText('No matching services yet')).toBeTruthy()
    expect(screen.getByRole('option', { name: 'Smartphones' })).toBeTruthy()
    expect(screen.getByRole('option', { name: 'Screen repair' })).toBeTruthy()
    expect(api.getCatalogServices).toHaveBeenCalledWith(expect.objectContaining({ size: 9, sort: 'name' }), expect.any(AbortSignal))
  })

  it('initializes filters from the URL and sends them to the API', async () => {
    renderCatalogue('/services?category=smartphones&q=screen&type=screen-repair&page=2&sort=name-desc')
    await waitFor(() => expect(api.getCatalogServices).toHaveBeenCalledWith(expect.objectContaining({
      category: 'smartphones', q: 'screen', type: 'screen-repair', page: 2, sort: 'name-desc', size: 9,
    }), expect.any(AbortSignal)))
    await waitFor(() => expect(screen.getByLabelText('Device').value).toBe('smartphones'))
    expect(screen.getByLabelText('Repair type').value).toBe('screen-repair')
  })

  it('shows a meaningful backend error', async () => {
    api.getCatalogServices.mockRejectedValue(new Error('Connection refused'))
    renderCatalogue()
    expect((await screen.findByRole('alert')).textContent).toContain('Connection refused')
  })

  it('searches the catalogue when the search form is submitted', async () => {
    renderCatalogue()
    const user = userEvent.setup()
    const input = screen.getByLabelText('Search services')
    await user.type(input, 'battery')
    await user.click(screen.getByRole('button', { name: 'Search' }))
    await waitFor(() => expect(api.getCatalogServices).toHaveBeenCalledWith(expect.objectContaining({ q: 'battery', page: 0 }), expect.any(AbortSignal)))
  })
})
