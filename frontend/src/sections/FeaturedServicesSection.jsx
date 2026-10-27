import { useEffect, useState } from 'react'
import { getCatalogServices } from '../api.js'
import ServiceCard from '../components/ServiceCard.jsx'
import SectionHeading from '../components/SectionHeading.jsx'
import Icon from '../components/Icon.jsx'

export default function FeaturedServicesSection() {
  const [state, setState] = useState({ status: 'loading' })
  useEffect(() => {
    const controller = new AbortController()
    getCatalogServices({ size: 3, sort: 'newest' }, controller.signal)
      .then((result) => setState({ status: 'ready', services: result.content }))
      .catch((error) => { if (error.name !== 'AbortError') setState({ status: 'error', message: error.message }) })
    return () => controller.abort()
  }, [])

  return (
    <section id="featured-services" className="section-space">
      <div className="page-container overflow-hidden rounded-[2rem] bg-ink px-5 py-10 text-white sm:px-8 sm:py-14 lg:px-12 lg:py-16">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <SectionHeading
            eyebrow="Repair service catalogue"
            title="Repair information, made easier to scan."
            description="Explore service descriptions organized by device and repair type."
            light
          />
          <span className="mb-1 inline-flex w-fit items-center gap-2 rounded-full border border-white/20 bg-white/10 px-3.5 py-2 text-xs font-semibold text-white/85">
            <span className="size-2 rounded-full bg-accent" /> Catalogue information
          </span>
        </div>
        {state.status === 'loading' && <div className="mt-8 grid gap-4 md:grid-cols-3" aria-label="Loading featured services">{[0, 1, 2].map((i) => <div key={i} className="h-72 animate-pulse rounded-3xl bg-white/10" />)}</div>}
        {state.status === 'error' && <p role="alert" className="mt-8 rounded-2xl border border-white/20 bg-white/10 p-5 text-sm text-white">The featured catalogue could not be loaded: {state.message}. <a className="font-semibold underline" href="/services">Open all services</a></p>}
        {state.status === 'ready' && <div className="mt-8 grid gap-4 md:grid-cols-2 xl:mt-11 xl:grid-cols-3">
          {state.services.map((service) => <ServiceCard key={service.id} service={service} />)}
        </div>}
        {state.status === 'ready' && state.services.length === 0 && <p className="mt-8 rounded-2xl border border-white/20 bg-white/10 p-5 text-sm text-white">No services are currently listed.</p>}
        <p className="mt-5 text-xs leading-5 text-white/65">Descriptions are general information only. Repair scope, compatible parts, availability and cost depend on a future device assessment.</p>
        <a href="/services" className="mt-6 inline-flex min-h-12 items-center gap-2 rounded-full bg-accent px-5 text-sm font-semibold text-ink hover:bg-accent-hover focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white">Browse all services <Icon name="arrow" size={17} /></a>
      </div>
    </section>
  )
}
