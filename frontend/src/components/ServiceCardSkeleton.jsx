export default function ServiceCardSkeleton() {
  return <div aria-hidden="true" className="animate-pulse overflow-hidden rounded-3xl border border-border bg-white">
    <div className="h-36 bg-surface-soft" />
    <div className="space-y-3 p-5"><div className="h-3 w-1/3 rounded bg-surface-soft" /><div className="h-5 w-3/4 rounded bg-surface-soft" /><div className="h-4 rounded bg-surface-soft" /><div className="h-4 w-5/6 rounded bg-surface-soft" /></div>
  </div>
}
