const variants = {
  primary: 'bg-ink text-white hover:bg-ink-hover',
  accent: 'bg-accent text-ink hover:bg-accent-hover',
  secondary: 'border border-border bg-white text-ink hover:border-ink/30 hover:bg-surface-soft',
  quiet: 'bg-transparent text-ink hover:bg-surface-soft',
}

export default function Button({ children, href, variant = 'primary', className = '', ...props }) {
  const classes = `inline-flex min-h-12 items-center justify-center gap-2 rounded-full px-5 text-sm font-semibold transition-colors duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-2 ${variants[variant]} ${className}`

  if (href) return <a className={classes} href={href} {...props}>{children}</a>
  return <button className={classes} {...props}>{children}</button>
}
