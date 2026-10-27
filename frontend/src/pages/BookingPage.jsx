import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { createBooking, getCatalogServiceBySlug, getCustomerGadgets } from '../api.js'
import AccountLayout from '../components/AccountLayout.jsx'
import Icon from '../components/Icon.jsx'

export default function BookingPage() {
  const { slug } = useParams()
  const navigate = useNavigate()

  const [service, setService] = useState(null)
  const [gadgets, setGadgets] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    gadgetId: '',
    serviceMode: 'HOME_VISIT',
    problemDescription: '',
  })

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')

    Promise.all([
      getCatalogServiceBySlug(slug),
      getCustomerGadgets(),
    ]).then(([serviceData, gadgetsData]) => {
      if (!active) return
      setService(serviceData)
      setGadgets(gadgetsData)

      // Pre-select first compatible gadget if available
      const compatible = gadgetsData.filter(g => g.category?.slug === serviceData.category?.slug)
      if (compatible.length > 0) {
        setForm(prev => ({ ...prev, gadgetId: String(compatible[0].id) }))
      }
    }).catch(err => {
      if (active) setError(err.message || 'Could not load booking details.')
    }).finally(() => {
      if (active) setLoading(false)
    })

    return () => { active = false }
  }, [slug])

  const compatibleGadgets = service ? gadgets.filter(g => g.category?.slug === service.category?.slug) : []

  async function submit(event) {
    event.preventDefault()
    setError('')

    if (!form.gadgetId) {
      setError('Please select a compatible gadget for this repair.')
      return
    }

    if (!form.problemDescription.trim() || form.problemDescription.trim().length < 10) {
      setError('Please describe the problem (at least 10 characters).')
      return
    }

    setBusy(true)
    try {
      await createBooking({
        gadgetId: Number(form.gadgetId),
        serviceId: service.id,
        serviceMode: form.serviceMode,
        problemDescription: form.problemDescription.trim(),
      })
      navigate('/account/bookings', { replace: true, state: { booked: true } })
    } catch (err) {
      setError(err.message || 'We could not submit your repair booking. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  if (loading) {
    return (
      <AccountLayout>
        <div className="mx-auto max-w-2xl py-12" role="status">
          <p className="text-secondary">Loading booking information…</p>
        </div>
      </AccountLayout>
    )
  }

  if (!service && error) {
    return (
      <AccountLayout>
        <div className="mx-auto max-w-2xl py-12" role="alert">
          <p className="text-error">{error}</p>
          <Link to="/services" className="mt-4 inline-block font-semibold text-teal underline">← Back to services</Link>
        </div>
      </AccountLayout>
    )
  }

  return (
    <AccountLayout>
      <div className="mx-auto max-w-3xl">
        <Link to={`/services/${encodeURIComponent(slug)}`} className="text-sm font-semibold text-secondary hover:text-ink">
          ← Back to {service?.name || 'service'}
        </Link>

        <div className="mt-4">
          <p className="text-sm font-bold uppercase tracking-[.14em] text-teal">Repair Booking</p>
          <h1 className="mt-2 text-3xl font-bold tracking-tight sm:text-4xl">Book {service.name}</h1>
          <p className="mt-2 text-secondary">
            Category: <span className="font-semibold text-ink">{service.category?.name}</span> · Repair type: <span className="font-semibold text-ink">{service.repairTypeLabel}</span>
          </p>
        </div>

        {error && (
          <div role="alert" className="mt-6 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-error">
            {error}
          </div>
        )}

        {compatibleGadgets.length === 0 ? (
          <div className="mt-8 rounded-3xl border border-dashed border-border bg-white p-8 text-center sm:p-12">
            <h2 className="text-xl font-bold">No compatible gadgets found</h2>
            <p className="mx-auto mt-2 max-w-md text-sm text-secondary">
              This service requires a registered <span className="font-semibold text-ink">{service.category?.name}</span>.
              {gadgets.length > 0
                ? " You have other gadgets registered, but none match this device category."
                : " You haven't registered any devices yet."}
            </p>
            <Link
              to="/account/gadgets/new"
              className="mt-6 inline-flex min-h-12 items-center justify-center rounded-full bg-accent px-6 text-sm font-bold text-ink"
            >
              Register a {service.category?.name} gadget
            </Link>
          </div>
        ) : (
          <form onSubmit={submit} className="mt-8 space-y-6 rounded-3xl border border-border bg-white p-6 shadow-card sm:p-9" noValidate>
            <div>
              <label htmlFor="gadget-select" className="block text-sm font-bold text-ink">
                Select your device <span className="text-teal">({service.category?.name})</span>
              </label>
              <select
                id="gadget-select"
                value={form.gadgetId}
                onChange={e => setForm({ ...form, gadgetId: e.target.value })}
                className="mt-2 min-h-12 w-full rounded-xl border border-border bg-white px-4 text-sm font-medium"
                required
              >
                <option value="">Select a compatible gadget</option>
                {compatibleGadgets.map(g => (
                  <option key={g.id} value={g.id}>
                    {g.brand} {g.model} {g.purchaseDate ? `(Purchased ${g.purchaseDate})` : ''}
                  </option>
                ))}
              </select>
              <p className="mt-2 text-xs text-secondary">
                Need to add another device? <Link to="/account/gadgets/new" className="font-semibold text-teal underline">Register a new gadget</Link>
              </p>
            </div>

            <div>
              <span className="block text-sm font-bold text-ink">Preferred service mode</span>
              <div className="mt-3 grid gap-3 sm:grid-cols-2">
                <label className={`flex cursor-pointer items-start gap-3 rounded-2xl border p-4 transition-colors ${form.serviceMode === 'HOME_VISIT' ? 'border-teal bg-teal/5' : 'border-border hover:bg-surface-soft'}`}>
                  <input
                    type="radio"
                    name="serviceMode"
                    value="HOME_VISIT"
                    checked={form.serviceMode === 'HOME_VISIT'}
                    onChange={() => setForm({ ...form, serviceMode: 'HOME_VISIT' })}
                    className="mt-1"
                  />
                  <div>
                    <span className="block text-sm font-bold text-ink">Home Visit</span>
                    <span className="mt-1 block text-xs text-secondary leading-5">Technician visits your location to inspect and perform the repair.</span>
                  </div>
                </label>

                <label className={`flex cursor-pointer items-start gap-3 rounded-2xl border p-4 transition-colors ${form.serviceMode === 'REPAIR_CENTRE' ? 'border-teal bg-teal/5' : 'border-border hover:bg-surface-soft'}`}>
                  <input
                    type="radio"
                    name="serviceMode"
                    value="REPAIR_CENTRE"
                    checked={form.serviceMode === 'REPAIR_CENTRE'}
                    onChange={() => setForm({ ...form, serviceMode: 'REPAIR_CENTRE' })}
                    className="mt-1"
                  />
                  <div>
                    <span className="block text-sm font-bold text-ink">Repair Centre</span>
                    <span className="mt-1 block text-xs text-secondary leading-5">Drop off your device at an authorized repair facility.</span>
                  </div>
                </label>
              </div>
            </div>

            <div>
              <label htmlFor="problem-desc" className="block text-sm font-bold text-ink">
                Problem description
              </label>
              <textarea
                id="problem-desc"
                rows={4}
                value={form.problemDescription}
                onChange={e => setForm({ ...form, problemDescription: e.target.value })}
                placeholder="Describe what happened, any symptoms, errors, or damage..."
                className="mt-2 w-full rounded-2xl border border-border p-4 text-sm"
                required
                minLength={10}
                maxLength={2000}
              />
              <span className="mt-1 block text-xs text-secondary">
                Please provide at least 10 characters so technicians can evaluate the repair.
              </span>
            </div>

            <div className="rounded-2xl border border-border bg-surface-soft p-4 text-xs text-secondary leading-5">
              <strong className="font-semibold text-ink">How matching works:</strong> Once you submit, our matching system checks verified active technicians who possess this specific repair skill and supported service mode, ranking candidates by eligibility, availability, and capacity. An offer is sent to the top match.
            </div>

            <div className="flex flex-wrap items-center gap-3 border-t border-border pt-5">
              <button
                type="submit"
                disabled={busy}
                className="inline-flex min-h-12 items-center justify-center rounded-full bg-accent px-8 text-sm font-bold text-ink disabled:opacity-50"
              >
                {busy ? 'Submitting booking…' : 'Submit Repair Booking'}
              </button>
              <Link
                to={`/services/${encodeURIComponent(slug)}`}
                className="inline-flex min-h-12 items-center justify-center rounded-full border border-border px-6 text-sm font-semibold text-secondary hover:bg-surface-soft"
              >
                Cancel
              </Link>
            </div>
          </form>
        )}
      </div>
    </AccountLayout>
  )
}
