import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router'
import { getCatalogServiceBySlug } from '../api.js'
import Icon from '../components/Icon.jsx'
import SiteFooter from '../components/SiteFooter.jsx'
import SiteHeader from '../components/SiteHeader.jsx'

const icons = { phone: 'phone', laptop: 'laptop', tablet: 'tablet', watch: 'watch', desktop: 'desktop' }

export default function ServiceDetailPage() {
  const { slug } = useParams()
  const location = useLocation()
  const [state, setState] = useState({ status: 'loading' })
  const [reloadVersion, setReloadVersion] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    setState({ status: 'loading' })
    getCatalogServiceBySlug(slug, controller.signal)
      .then((service) => setState({ status: 'ready', service }))
      .catch((error) => { if (error.name !== 'AbortError') setState({ status: 'error', message: error.message, notFound: error.status === 404 }) })
    return () => controller.abort()
  }, [slug, reloadVersion])
  const from = location.state?.from
  const backTo = typeof from === 'string' && from.startsWith('/services') && !from.startsWith('//') ? from : '/services'
  const service = state.service
  return <><SiteHeader /><main id="main-content" className="min-h-[65vh] bg-page py-8 sm:py-12"><div className="page-container max-w-5xl">
    <Link to={backTo} className="inline-flex min-h-11 items-center gap-2 rounded-full px-4 text-sm font-semibold text-secondary hover:bg-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal"><span aria-hidden="true">←</span> Back to services</Link>
    {state.status === 'loading' && <div className="mt-5 animate-pulse rounded-3xl border border-border bg-white p-8 sm:p-12" aria-label="Loading service details"><div className="h-4 w-32 rounded bg-surface-soft" /><div className="mt-5 h-9 max-w-lg rounded bg-surface-soft" /><div className="mt-5 h-20 max-w-2xl rounded bg-surface-soft" /></div>}
    {state.status === 'error' && <div className="mt-5 rounded-3xl border border-border bg-white p-8 sm:p-12" role={state.notFound ? 'status' : 'alert'}><p className="text-xs font-bold uppercase tracking-[0.15em] text-teal">{state.notFound ? 'Service not found' : 'Catalogue unavailable'}</p><h1 className="mt-3 text-2xl font-semibold text-ink">{state.notFound ? 'This service is no longer available.' : 'We couldn’t load this service.'}</h1><p className="mt-2 text-sm leading-6 text-secondary">{state.message}</p>{!state.notFound && <button onClick={() => setReloadVersion((version) => version + 1)} className="mt-5 rounded-full bg-ink px-5 py-3 text-sm font-semibold text-white">Try again</button>}</div>}
    {service && <article className="mt-5 overflow-hidden rounded-3xl border border-border bg-white shadow-card">
      <div className="bg-ink px-6 py-10 text-white sm:px-10 sm:py-14"><p className="text-sm font-semibold text-accent">{service.category.name} <span className="px-1 text-white/40">/</span> {service.repairTypeLabel}</p><h1 className="mt-4 max-w-3xl text-3xl font-semibold tracking-[-0.045em] sm:text-4xl">{service.name}</h1><p className="mt-4 max-w-3xl text-base leading-7 text-white/75">{service.shortDescription}</p></div>
      <div className="grid gap-9 p-6 sm:p-10 lg:grid-cols-[1fr_19rem]">
        <div><h2 className="text-lg font-semibold text-ink">About this service</h2><p className="mt-3 text-sm leading-7 text-secondary">{service.detailedDescription}</p><div className="mt-8 rounded-2xl border border-warning/20 bg-warning/5 p-5"><h2 className="text-sm font-semibold text-ink">Before you decide</h2><p className="mt-2 text-sm leading-6 text-secondary">This listing provides general information. It is not a diagnosis, confirmed repair offer, booking, or price quote. Device condition, compatible parts, repair scope, availability, and cost must be assessed before any work is agreed.</p></div></div>
        <aside className="h-fit rounded-2xl bg-page p-5"><span className="grid size-12 place-items-center rounded-2xl bg-white text-ink"><Icon name={icons[service.category.iconId] || 'sparkle'} size={27} /></span><h2 className="mt-4 font-semibold text-ink">Service overview</h2><dl className="mt-4 space-y-4 text-sm"><div><dt className="text-muted">Device category</dt><dd className="mt-1 font-medium text-ink">{service.category.name}</dd></div><div><dt className="text-muted">Repair type</dt><dd className="mt-1 font-medium text-ink">{service.repairTypeLabel}</dd></div></dl><Link to={`/services/${encodeURIComponent(service.slug)}/book`} className="mt-6 inline-flex min-h-12 w-full items-center justify-center gap-2 rounded-full bg-accent px-5 text-sm font-bold text-ink hover:brightness-105 shadow-sm">Book Repair <Icon name="arrow" size={17} /></Link><Link to="/services" className="mt-3 inline-flex min-h-11 w-full items-center justify-center gap-2 rounded-full border border-border bg-white px-4 text-sm font-semibold text-ink hover:bg-surface-soft">Explore more services <Icon name="arrow" size={17} /></Link></aside>
      </div>
    </article>}
  </div></main><SiteFooter /></>
}
