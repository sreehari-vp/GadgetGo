import Icon from './Icon.jsx'

const artTones = {
  phone: 'bg-[#edf5d4] text-ink',
  laptop: 'bg-[#e7eef6] text-ink',
  tablet: 'bg-[#e0efeb] text-ink',
}

export default function ServicePreviewCard({ service }) {
  return (
    <article className="preview-card overflow-hidden rounded-[1.5rem] border border-white/10 bg-white text-ink shadow-card">
      <div className={`relative flex h-44 items-center justify-center overflow-hidden ${artTones[service.art] ?? 'bg-surface-soft text-ink'}`}>
        <div aria-hidden="true" className="absolute -right-9 -top-12 size-40 rounded-full border-[22px] border-white/35" />
        <div aria-hidden="true" className="absolute -bottom-16 -left-8 size-44 rounded-full bg-white/30" />
        <span className="relative grid size-[5.25rem] place-items-center rounded-[1.7rem] bg-white/80 shadow-card"><Icon name={service.icon} size={43} /></span>
        <span className="absolute left-4 top-4 rounded-full bg-white/90 px-3 py-1.5 text-[0.67rem] font-bold uppercase tracking-[0.13em] text-secondary">Preview</span>
      </div>
      <div className="p-5 sm:p-6">
        <p className="text-xs font-semibold uppercase tracking-[0.13em] text-teal">{service.category}</p>
        <h3 className="mt-2 text-xl font-semibold tracking-[-0.025em]">{service.name}</h3>
        <p className="mt-2 min-h-[3.25rem] text-sm leading-6 text-secondary">{service.description}</p>
        <p className="mt-5 border-t border-border pt-4 text-xs font-medium text-muted">Illustrative content · not a live service</p>
      </div>
    </article>
  )
}
