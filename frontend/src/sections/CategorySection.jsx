import { useEffect, useState } from 'react'
import { getCatalogCategories } from '../api.js'
import CategoryCard from '../components/CategoryCard.jsx'
import SectionHeading from '../components/SectionHeading.jsx'

const categoryTones = ['lime', 'blue', 'teal', 'rose', 'sand']

function categoryIcon(iconId) {
  return ({ smartphones: 'phone', laptops: 'laptop', tablets: 'tablet', smartwatches: 'watch', desktop: 'desktop' })[iconId] || 'sparkle'
}

export default function CategorySection() {
  const [state, setState] = useState({ status: 'loading' })
  useEffect(() => {
    const controller = new AbortController()
    getCatalogCategories(controller.signal)
      .then((categories) => setState({ status: 'ready', categories }))
      .catch((error) => { if (error.name !== 'AbortError') setState({ status: 'error', message: error.message }) })
    return () => controller.abort()
  }, [])

  return (
    <section id="device-categories" className="section-space bg-white">
      <div className="page-container">
        <div className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
          <SectionHeading
            eyebrow="Start with your device"
            title="Find a service for your kind of tech."
            description="Browse the current catalogue by the device you want to repair."
          />
          <a href="/services" className="mb-1 inline-flex min-h-11 items-center rounded-full border border-border bg-white px-5 text-sm font-semibold text-ink hover:bg-surface-soft">Browse all services</a>
        </div>
        {state.status === 'loading' && <div className="mt-9 grid gap-3 sm:grid-cols-2 lg:mt-12 lg:grid-cols-3 lg:gap-4" aria-label="Loading device categories">{Array.from({ length: 5 }, (_, i) => <div key={i} aria-hidden="true" className="h-32 animate-pulse rounded-2xl bg-surface-soft" />)}</div>}
        {state.status === 'error' && <p role="alert" className="mt-7 rounded-xl border border-error/30 bg-white p-4 text-sm text-error">Device categories could not be loaded: {state.message}. <a className="font-semibold underline" href="/services">Open the catalogue</a></p>}
        {state.status === 'ready' && <div className="mt-9 grid gap-3 sm:grid-cols-2 lg:mt-12 lg:grid-cols-3 lg:gap-4">
          {state.categories.map((category, index) => <CategoryCard key={category.id} category={{ ...category, icon: categoryIcon(category.iconId), tone: categoryTones[index % categoryTones.length] }} />)}
        </div>}
      </div>
    </section>
  )
}
