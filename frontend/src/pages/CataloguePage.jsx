import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router'
import { getCatalogCategories, getCatalogRepairTypes, getCatalogServices } from '../api.js'
import Icon from '../components/Icon.jsx'
import ServiceCard from '../components/ServiceCard.jsx'
import ServiceCardSkeleton from '../components/ServiceCardSkeleton.jsx'
import SiteFooter from '../components/SiteFooter.jsx'
import SiteHeader from '../components/SiteHeader.jsx'

const PAGE_SIZE = 9
const SORTS = [
  ['name', 'Name A–Z'], ['name-desc', 'Name Z–A'], ['type', 'Repair type'], ['newest', 'Recently added'], ['oldest', 'First added'],
]

export default function CataloguePage() {
  const [params, setParams] = useSearchParams()
  const [draftQuery, setDraftQuery] = useState(params.get('q') || '')
  const [categories, setCategories] = useState([])
  const [types, setTypes] = useState([])
  const [catalogState, setCatalogState] = useState({ status: 'loading' })
  const [filtersState, setFiltersState] = useState({ status: 'loading' })
  const [reloadVersion, setReloadVersion] = useState(0)
  const filters = useMemo(() => ({
    q: params.get('q') || '', category: params.get('category') || '', type: params.get('type') || '',
    sort: params.get('sort') || 'name', page: Number(params.get('page') || '0'),
  }), [params])

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([getCatalogCategories(controller.signal), getCatalogRepairTypes(controller.signal)])
      .then(([nextCategories, nextTypes]) => {
        setCategories(nextCategories); setTypes(nextTypes); setFiltersState({ status: 'ready' })
      })
      .catch((error) => { if (error.name !== 'AbortError') setFiltersState({ status: 'error', message: error.message }) })
    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    setCatalogState((current) => ({ status: current.status === 'ready' ? 'refreshing' : 'loading' }))
    const timer = window.setTimeout(() => {
      getCatalogServices({ ...filters, size: PAGE_SIZE }, controller.signal)
        .then((result) => setCatalogState({ status: 'ready', result }))
        .catch((error) => { if (error.name !== 'AbortError') setCatalogState({ status: 'error', message: error.message }) })
    }, 180)
    return () => { window.clearTimeout(timer); controller.abort() }
  }, [filters, reloadVersion])

  useEffect(() => setDraftQuery(params.get('q') || ''), [params])

  function update(next, { resetPage = true } = {}) {
    const updated = new URLSearchParams(params)
    for (const [key, value] of Object.entries(next)) {
      if (value === '' || value == null || (key === 'sort' && value === 'name')) updated.delete(key)
      else updated.set(key, String(value))
    }
    if (resetPage && !Object.hasOwn(next, 'page')) updated.delete('page')
    setParams(updated)
  }

  const result = catalogState.result
  const loading = catalogState.status === 'loading' || catalogState.status === 'refreshing'
  return <><SiteHeader /><main id="main-content" className="min-h-[65vh] bg-page">
    <section className="border-b border-border bg-white py-10 sm:py-14">
      <div className="page-container">
        <p className="text-xs font-bold uppercase tracking-[0.16em] text-teal">Repair service catalogue</p>
        <h1 className="mt-3 text-3xl font-semibold tracking-[-0.045em] text-ink sm:text-4xl">Find a service for your device.</h1>
        <p className="mt-3 max-w-2xl text-base leading-7 text-secondary">Explore repair information by device or issue. A listing is a starting point for understanding options, not a confirmed diagnosis or quote.</p>
        <form role="search" className="mt-7 flex max-w-3xl gap-2 rounded-2xl border border-border bg-page p-2 sm:rounded-full" onSubmit={(event) => { event.preventDefault(); update({ q: draftQuery.trim() }) }}>
          <label className="sr-only" htmlFor="catalog-search">Search services</label>
          <Icon name="search" size={21} className="my-auto ml-3 shrink-0 text-muted" />
          <input id="catalog-search" value={draftQuery} onChange={(event) => setDraftQuery(event.target.value)} placeholder="Search a device or repair" className="h-11 min-w-0 flex-1 bg-transparent text-sm text-ink outline-none placeholder:text-muted" />
          <button className="min-h-11 rounded-full bg-ink px-5 text-sm font-semibold text-white hover:bg-ink-hover focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal">Search</button>
        </form>
      </div>
    </section>

    <section className="page-container py-8 sm:py-10" aria-label="Repair service results">
      <div className="grid gap-3 rounded-2xl border border-border bg-white p-4 sm:grid-cols-2 lg:grid-cols-[1fr_1fr_1fr_auto] lg:items-end">
        <div><label className="mb-1.5 block text-xs font-semibold text-secondary" htmlFor="category-filter">Device</label><select id="category-filter" className="h-11 w-full rounded-xl border border-border bg-white px-3 text-sm text-ink" value={filters.category} onChange={(event) => update({ category: event.target.value })}><option value="">All devices</option>{categories.map((category) => <option key={category.id} value={category.slug}>{category.name}</option>)}</select></div>
        <div><label className="mb-1.5 block text-xs font-semibold text-secondary" htmlFor="type-filter">Repair type</label><select id="type-filter" className="h-11 w-full rounded-xl border border-border bg-white px-3 text-sm text-ink" value={filters.type} onChange={(event) => update({ type: event.target.value })}><option value="">All repair types</option>{types.map((type) => <option key={type.slug} value={type.slug}>{type.label}</option>)}</select></div>
        <div><label className="mb-1.5 block text-xs font-semibold text-secondary" htmlFor="sort-filter">Sort by</label><select id="sort-filter" className="h-11 w-full rounded-xl border border-border bg-white px-3 text-sm text-ink" value={filters.sort} onChange={(event) => update({ sort: event.target.value })}>{SORTS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div>
        <button type="button" onClick={() => { setDraftQuery(''); setParams(new URLSearchParams()) }} className="min-h-11 rounded-full px-4 text-sm font-semibold text-secondary hover:bg-surface-soft">Clear filters</button>
      </div>
      {filtersState.status === 'error' && <p role="status" className="mt-4 rounded-xl border border-warning/30 bg-warning/5 p-3 text-sm text-warning">Filter options could not be loaded: {filtersState.message}</p>}
      <div className="mt-7 flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-secondary" aria-live="polite">{loading ? 'Loading services…' : catalogState.status === 'error' ? 'Services unavailable' : `${result?.totalElements ?? 0} ${result?.totalElements === 1 ? 'service' : 'services'}${filters.q ? ` for “${filters.q}”` : ''}`}</p>
        {(filters.category || filters.type || filters.q) && <button onClick={() => { setDraftQuery(''); setParams(new URLSearchParams()) }} className="text-sm font-semibold text-teal hover:underline">Clear active filters</button>}
      </div>

      {catalogState.status === 'error' && <div className="mt-5 rounded-2xl border border-error/30 bg-white p-6 text-sm text-error" role="alert"><p className="font-semibold">We couldn’t load the service catalogue.</p><p className="mt-1">{catalogState.message} Check your connection and try again.</p><button onClick={() => setReloadVersion((version) => version + 1)} className="mt-3 rounded-full px-3 py-2 font-semibold text-ink hover:bg-surface-soft">Try again</button></div>}
      {loading && <div className="mt-5 grid gap-4 sm:grid-cols-2 xl:grid-cols-3" aria-label="Loading repair services">{Array.from({ length: 6 }, (_, index) => <ServiceCardSkeleton key={index} />)}</div>}
      {catalogState.status === 'ready' && result?.content.length === 0 && <div className="mt-5 rounded-3xl border border-border bg-white px-6 py-14 text-center"><span className="mx-auto grid size-14 place-items-center rounded-2xl bg-surface-soft"><Icon name="search" /></span><h2 className="mt-4 text-xl font-semibold text-ink">No matching services yet</h2><p className="mx-auto mt-2 max-w-md text-sm leading-6 text-secondary">Try another search or remove a filter to see more catalogue information.</p><button onClick={() => { setDraftQuery(''); setParams(new URLSearchParams()) }} className="mt-5 min-h-11 rounded-full bg-ink px-5 text-sm font-semibold text-white">Show all services</button></div>}
      {result?.content.length > 0 && <div className={`mt-5 grid gap-4 sm:grid-cols-2 xl:grid-cols-3 ${loading ? 'opacity-55' : ''}`} aria-busy={loading}>{result.content.map((service) => <ServiceCard key={service.id} service={service} />)}</div>}
      {result?.totalPages > 1 && <nav aria-label="Catalogue pages" className="mt-9 flex items-center justify-center gap-2"><button disabled={result.first} onClick={() => update({ page: filters.page - 1 }, { resetPage: false })} className="min-h-11 rounded-full border border-border bg-white px-4 text-sm font-semibold disabled:opacity-40">Previous</button><span className="px-3 text-sm text-secondary">Page {result.page + 1} of {result.totalPages}</span><button disabled={result.last} onClick={() => update({ page: filters.page + 1 }, { resetPage: false })} className="min-h-11 rounded-full border border-border bg-white px-4 text-sm font-semibold disabled:opacity-40">Next</button></nav>}
      <p className="mt-8 text-xs leading-5 text-muted">Service descriptions are general catalogue information. The exact work, compatible parts, availability, and cost require a future device assessment.</p>
    </section>
  </main><SiteFooter /></>
}
