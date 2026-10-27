export default function SectionHeading({ eyebrow, title, description, align = 'left', light = false }) {
  const alignment = align === 'center' ? 'mx-auto text-center' : ''
  const titleColor = light ? 'text-white' : 'text-ink'
  const descriptionColor = light ? 'text-white/70' : 'text-secondary'

  return (
    <div className={`max-w-2xl ${alignment}`}>
      {eyebrow && <p className={`mb-3 text-xs font-bold uppercase tracking-[0.18em] ${light ? 'text-accent' : 'text-teal'}`}>{eyebrow}</p>}
      <h2 className={`text-3xl font-semibold leading-tight tracking-[-0.035em] sm:text-4xl ${titleColor}`}>{title}</h2>
      {description && <p className={`mt-4 max-w-xl text-base leading-7 sm:text-lg ${descriptionColor} ${align === 'center' ? 'mx-auto' : ''}`}>{description}</p>}
    </div>
  )
}
