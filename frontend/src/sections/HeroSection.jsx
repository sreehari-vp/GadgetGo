import { useState } from 'react'
import { useNavigate } from 'react-router'
import Button from '../components/Button.jsx'
import Icon from '../components/Icon.jsx'

export default function HeroSection() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')

  function handleSubmit(event) {
    event.preventDefault()
    const searchTerm = query.trim()
    navigate(searchTerm ? `/services?q=${encodeURIComponent(searchTerm)}` : '/services')
  }

  return (
    <section id="top" className="overflow-hidden bg-page">
      <div className="page-container grid items-center gap-10 pb-14 pt-10 sm:pb-16 sm:pt-14 lg:grid-cols-[1.02fr_0.98fr] lg:gap-12 lg:pb-20 lg:pt-16">
        <div className="relative z-10">
          <p className="inline-flex items-center gap-2 rounded-full border border-border bg-white px-3.5 py-2 text-xs font-semibold text-secondary shadow-card sm:text-sm">
            <span className="size-2 rounded-full bg-accent" /> A clearer start for device repair
          </p>
          <h1 className="mt-6 max-w-[14ch] text-[clamp(2.35rem,4.85vw,4.35rem)] font-semibold leading-[1.05] tracking-[-0.06em] text-ink">
            <span className="block">Your tech</span>
            <span className="block">deserves better.</span>
            <span className="block">Let’s get it <span className="text-teal">fixed.</span></span>
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-secondary sm:text-lg sm:leading-8">
            Find a clearer way to explore repair services for your phone, laptop, and the devices you rely on every day.
          </p>

          <form role="search" aria-label="Search repair services" onSubmit={handleSubmit} className="mt-8 flex flex-col gap-3 rounded-[1.4rem] border border-border/80 bg-white p-3 shadow-soft sm:flex-row sm:items-center sm:rounded-full sm:p-2 sm:pl-5">
            <Icon name="search" size={21} className="hidden shrink-0 text-muted sm:block" />
            <label className="sr-only" htmlFor="service-search">What device or repair are you looking for?</label>
            <input
              id="service-search"
              name="q"
              type="search"
              autoComplete="off"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="What needs fixing?"
              className="h-12 min-w-0 flex-1 rounded-full border-0 bg-transparent px-3 text-sm text-ink outline-none placeholder:text-muted focus-visible:ring-2 focus-visible:ring-teal sm:px-0 sm:focus-visible:ring-0"
              aria-describedby="search-hint"
            />
            <Button variant="primary" type="submit" className="w-full sm:w-auto sm:px-6">
              Search services <Icon name="arrow" size={17} />
            </Button>
          </form>
          <p id="search-hint" className="mt-3 text-xs leading-5 text-muted">Search the current repair service catalogue.</p>

          <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center">
            <Button href="/services" variant="accent" className="min-h-[52px] px-6">Browse services <Icon name="arrow" size={18} /></Button>
            <Button href="#how-it-works" variant="quiet" className="min-h-[52px] px-5">How GadgetGo is designed</Button>
          </div>
          <p className="mt-7 text-xs font-semibold uppercase tracking-[0.16em] text-muted">Your Gadget. Fixed. Simple.</p>
        </div>

        <div className="relative mx-auto w-full max-w-[43rem] lg:ml-auto">
          <div aria-hidden="true" className="absolute -right-6 -top-7 size-28 rounded-full bg-accent/60 blur-2xl sm:-right-8 sm:size-40" />
          <div aria-hidden="true" className="absolute -bottom-5 -left-5 size-28 rounded-full bg-teal/15 blur-2xl" />
          <div className="relative overflow-hidden rounded-[1.7rem] bg-ink shadow-soft sm:rounded-[2rem]">
            <img
              src="/images/repair-hero.png"
              alt="A technician carefully repairing an unbranded smartphone at a workbench."
              className="aspect-[1.16/1] w-full object-cover object-[56%_center] sm:aspect-[1.18/1] lg:aspect-[0.98/1]"
              fetchPriority="high"
              width="1536"
              height="1024"
            />
            <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-ink/90 via-ink/35 to-transparent px-5 pb-5 pt-16 sm:px-7 sm:pb-7">
              <p className="text-sm font-semibold text-white sm:text-base">Careful work starts with clear information.</p>
              <p className="mt-1 text-xs text-white/75 sm:text-sm">GadgetGo is building a simpler place to explore repair options.</p>
            </div>
          </div>
          <div className="absolute -left-2 top-5 flex items-center gap-3 rounded-2xl border border-white/70 bg-white px-3.5 py-3 shadow-soft sm:-left-8 sm:top-10 sm:px-4">
            <span className="grid size-10 place-items-center rounded-xl bg-[#edf5d4] text-ink"><Icon name="tool" size={21} /></span>
            <span><span className="block text-xs font-bold text-ink sm:text-sm">Thoughtful discovery</span><span className="mt-0.5 block text-[0.68rem] text-muted">Repair options, clearly explained</span></span>
          </div>
          <p className="mt-3 text-right text-[0.68rem] text-muted">Original GadgetGo concept image</p>
        </div>
      </div>
    </section>
  )
}
