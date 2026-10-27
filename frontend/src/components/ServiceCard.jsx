import { Link, useLocation } from 'react-router'
import Icon from './Icon.jsx'

const categoryIcons = { phone: 'phone', laptop: 'laptop', tablet: 'tablet', watch: 'watch', desktop: 'desktop' }

export default function ServiceCard({ service }) {
  const location = useLocation()
  const icon = categoryIcons[service.category?.iconId] || 'sparkle'
  return (
    <article className="overflow-hidden rounded-3xl border border-border bg-white shadow-card transition-shadow hover:shadow-soft">
      <div className="flex h-36 items-center justify-between bg-surface-soft px-6">
        <span className="grid size-14 place-items-center rounded-2xl bg-white text-ink shadow-card"><Icon name={icon} size={30} /></span>
        <span className="rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-secondary">{service.category.name}</span>
      </div>
      <div className="p-5">
        <p className="text-xs font-bold uppercase tracking-[0.12em] text-teal">{service.repairTypeLabel}</p>
        <h2 className="mt-2 text-lg font-semibold tracking-tight text-ink">{service.name}</h2>
        <p className="mt-2 min-h-12 text-sm leading-6 text-secondary">{service.shortDescription}</p>
        <Link className="mt-5 inline-flex min-h-11 items-center gap-2 rounded-full px-4 text-sm font-semibold text-ink hover:bg-surface-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal" to={`/services/${service.slug}`} state={{ from: `${location.pathname}${location.search}` }}>
          View service <Icon name="arrow" size={17} />
        </Link>
      </div>
    </article>
  )
}
