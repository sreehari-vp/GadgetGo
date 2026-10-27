import Icon from './Icon.jsx'
import { Link } from 'react-router'

const tones = {
  lime: 'bg-[#edf5d4] text-ink',
  blue: 'bg-[#e7eef6] text-ink',
  teal: 'bg-[#e0efeb] text-ink',
  sand: 'bg-[#f4ebdc] text-ink',
  rose: 'bg-[#f4e8e6] text-ink',
  slate: 'bg-[#e9edf0] text-ink',
}

export default function CategoryCard({ category }) {
  return (
    <Link to={`/services?category=${encodeURIComponent(category.slug)}`} className="group flex min-h-[126px] items-center gap-4 rounded-2xl border border-border/80 bg-white p-5 shadow-card transition-shadow hover:shadow-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal">
      <span className={`grid size-14 shrink-0 place-items-center rounded-2xl ${tones[category.tone] ?? tones.slate}`}><Icon name={category.icon} size={27} /></span>
      <span className="min-w-0">
        <span className="block font-semibold tracking-[-0.015em] text-ink">{category.name}</span>
        <span className="mt-1 block text-sm leading-5 text-secondary">{category.description}</span>
      </span>
      <Icon name="arrow" size={18} className="ml-auto shrink-0 text-muted transition-transform group-hover:translate-x-1" />
    </Link>
  )
}
