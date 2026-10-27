import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router'
import { getCustomerBookings, rematchBooking } from '../api.js'
import AccountLayout from '../components/AccountLayout.jsx'

const statusBadges = {
  PENDING_MATCH: { label: 'Finding technician', bg: 'bg-amber-50 text-amber-800 border-amber-200' },
  OFFERED: { label: 'Offer in review', bg: 'bg-blue-50 text-blue-800 border-blue-200' },
  ASSIGNED: { label: 'Technician assigned', bg: 'bg-teal/10 text-teal border-teal/20' },
  NO_MATCH: { label: 'No match currently', bg: 'bg-gray-100 text-gray-700 border-gray-200' },
  CANCELLED: { label: 'Cancelled', bg: 'bg-red-50 text-red-700 border-red-200' },
}

function AssignmentProgress({ status, onRetry, retrying }) {
  const steps = [
    { key: 'REQUESTED', label: 'Requested' },
    { key: 'MATCHING', label: 'Matching' },
    { key: 'ASSIGNED', label: 'Assigned' },
    { key: 'IN_SERVICE', label: 'In Service' },
  ]

  let activeIndex = 0
  const isNoMatch = status === 'NO_MATCH'
  const isCancelled = status === 'CANCELLED'

  if (status === 'PENDING_MATCH') activeIndex = 1
  else if (status === 'OFFERED') activeIndex = 1
  else if (status === 'ASSIGNED') activeIndex = 2

  if (isCancelled) {
    return (
      <div className="my-3 rounded-2xl border border-red-200 bg-red-50/70 p-3 text-xs font-semibold text-error">
        Booking has been cancelled.
      </div>
    )
  }

  if (isNoMatch) {
    return (
      <div className="my-3 rounded-2xl border border-amber-200 bg-amber-50 p-4 text-xs text-amber-900 leading-relaxed flex flex-wrap items-center justify-between gap-3">
        <div className="max-w-xl">
          <p className="font-bold text-amber-950">No technician match available</p>
          <p className="mt-1 text-amber-800">
            All eligible technicians with the required skill are currently at maximum capacity or unavailable for this service mode. Our operations team has been notified, or you may try retrying matching.
          </p>
        </div>
        {onRetry && (
          <button
            onClick={onRetry}
            disabled={retrying}
            className="inline-flex min-h-8 items-center justify-center rounded-full bg-amber-900 px-4 text-xs font-bold text-white hover:bg-amber-950 disabled:opacity-50 transition-all shadow-sm"
          >
            {retrying ? 'Retrying…' : 'Retry Matching'}
          </button>
        )}
      </div>
    )
  }

  return (
    <div className="my-5 rounded-2xl border border-border bg-surface-soft/60 p-4">
      <div className="flex items-center justify-between">
        {steps.map((step, idx) => {
          const isCompleted = idx < activeIndex
          const isCurrent = idx === activeIndex
          return (
            <div key={step.key} className="flex flex-1 items-center">
              <div className="flex flex-col items-center text-center">
                <div
                  className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold transition-all ${
                    isCompleted
                      ? 'bg-teal text-white shadow-sm'
                      : isCurrent
                      ? 'bg-accent text-ink ring-4 ring-accent/30 font-extrabold'
                      : 'border border-border bg-white text-secondary'
                  }`}
                >
                  {isCompleted ? '✓' : idx + 1}
                </div>
                <span className={`mt-1.5 text-[11px] tracking-tight ${isCurrent ? 'font-bold text-ink' : 'text-secondary font-medium'}`}>
                  {step.label}
                </span>
              </div>
              {idx < steps.length - 1 && (
                <div
                  className={`mx-2 h-1 flex-1 rounded-full transition-colors ${
                    idx < activeIndex ? 'bg-teal' : 'bg-border'
                  }`}
                />
              )}
            </div>
          )
        })}
      </div>
      {status === 'OFFERED' && (
        <p className="mt-3 text-center text-xs font-medium text-secondary">
          Offer dispatched to top-ranked certified technician. Awaiting response…
        </p>
      )}
      {status === 'ASSIGNED' && (
        <p className="mt-3 text-center text-xs font-semibold text-teal">
          ✓ Certified technician confirmed and scheduled.
        </p>
      )}
    </div>
  )
}

function BookingSkeleton() {
  return (
    <div className="space-y-4">
      {[1, 2].map(n => (
        <div key={n} className="animate-pulse rounded-3xl border border-border bg-white p-6 shadow-card">
          <div className="flex justify-between border-b border-border pb-4">
            <div className="h-6 w-48 rounded bg-surface-soft" />
            <div className="h-6 w-28 rounded-full bg-surface-soft" />
          </div>
          <div className="mt-4 grid gap-4 sm:grid-cols-3">
            <div className="h-12 rounded bg-surface-soft" />
            <div className="h-12 rounded bg-surface-soft" />
            <div className="h-12 rounded bg-surface-soft" />
          </div>
          <div className="mt-4 h-16 rounded-xl bg-surface-soft" />
        </div>
      ))}
    </div>
  )
}

export default function CustomerBookingsPage() {
  const location = useLocation()
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [justBooked, setJustBooked] = useState(Boolean(location.state?.booked))
  const [retryingBookingId, setRetryingBookingId] = useState(null)

  const loadBookings = () => {
    setLoading(true)
    setError('')
    getCustomerBookings()
      .then(data => {
        setBookings(data)
      })
      .catch(err => {
        setError(err.message || 'Could not load your bookings.')
      })
      .finally(() => {
        setLoading(false)
      })
  }

  const handleRetryMatching = async (bookingId) => {
    setRetryingBookingId(bookingId)
    setError('')
    try {
      await rematchBooking(bookingId)
      loadBookings()
    } catch (err) {
      setError(err.message || 'Could not rematch this booking.')
    } finally {
      setRetryingBookingId(null)
    }
  }

  useEffect(() => {
    loadBookings()
  }, [])

  return (
    <AccountLayout>
      <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-bold uppercase tracking-[.14em] text-teal">Your account</p>
          <h1 className="mt-2 text-3xl font-bold tracking-tight sm:text-4xl">My Bookings</h1>
          <p className="mt-2 text-secondary">Track your repair requests, smart technician matching, and assignment progress.</p>
        </div>
        <Link
          to="/services"
          className="inline-flex min-h-12 items-center justify-center rounded-full bg-accent px-6 text-sm font-bold text-ink hover:brightness-105"
        >
          Explore repair services
        </Link>
      </div>

      {justBooked && (
        <div role="status" className="mt-6 flex items-center justify-between rounded-2xl border border-teal/30 bg-teal/5 p-4 text-sm text-ink shadow-sm">
          <div className="flex items-center gap-2">
            <span className="flex h-6 w-6 items-center justify-center rounded-full bg-teal text-xs text-white">✓</span>
            <span>Your repair booking was created successfully! Smart technician matching is underway.</span>
          </div>
          <button onClick={() => setJustBooked(false)} className="ml-4 text-xs font-bold text-teal hover:underline">Dismiss</button>
        </div>
      )}

      {error && (
        <div role="alert" className="mt-6 flex items-center justify-between rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-error">
          <span>{error}</span>
          <button onClick={loadBookings} className="ml-4 font-bold underline hover:no-underline">Retry</button>
        </div>
      )}

      {loading && (
        <div className="mt-8">
          <p role="status" className="sr-only">Loading your repair bookings…</p>
          <BookingSkeleton />
        </div>
      )}

      {!loading && !error && bookings.length === 0 && (
        <section className="mt-8 rounded-3xl border border-dashed border-border bg-white px-6 py-12 text-center sm:py-16">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-surface-soft text-2xl text-secondary">
            🔧
          </div>
          <h2 className="mt-4 text-xl font-bold text-ink">No repair bookings yet</h2>
          <p className="mx-auto mt-2 max-w-md text-sm text-secondary">
            When you need a device fixed, choose a service from our catalogue and book an appointment with a verified technician.
          </p>
          <Link
            to="/services"
            className="mt-6 inline-flex min-h-12 items-center justify-center rounded-full bg-ink px-6 text-sm font-bold text-white hover:bg-ink-hover"
          >
            Browse repair services
          </Link>
        </section>
      )}

      {!loading && bookings.length > 0 && (
        <div className="mt-8 space-y-5">
          {bookings.map(booking => {
            const badge = statusBadges[booking.status] || { label: booking.status, bg: 'bg-gray-100 text-gray-800 border-gray-200' }
            const date = booking.createdAt ? new Date(booking.createdAt).toLocaleDateString(undefined, {
              year: 'numeric',
              month: 'short',
              day: 'numeric',
            }) : 'Recent'

            return (
              <article
                key={booking.id}
                className="rounded-3xl border border-border bg-white p-6 shadow-card transition-all hover:shadow-md sm:p-7"
              >
                <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border pb-4">
                  <div>
                    <span className="text-xs font-semibold uppercase tracking-wider text-secondary">
                      Booking #{booking.id} · {date}
                    </span>
                    <h2 className="mt-1 text-xl font-bold text-ink">{booking.service?.name}</h2>
                  </div>
                  <span className={`inline-flex items-center rounded-full border px-3.5 py-1 text-xs font-bold ${badge.bg}`}>
                    {badge.label}
                  </span>
                </div>

                {/* Assignment Progress Visual Stepper */}
                <AssignmentProgress
                  status={booking.status}
                  onRetry={() => handleRetryMatching(booking.id)}
                  retrying={retryingBookingId === booking.id}
                />

                <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  <div className="rounded-2xl border border-border/80 bg-surface-soft/40 p-4">
                    <span className="text-xs font-medium text-secondary">Device</span>
                    <p className="mt-1 font-semibold text-ink">{booking.gadget?.brand} {booking.gadget?.model}</p>
                    <span className="text-xs text-secondary">{booking.gadget?.categoryName}</span>
                  </div>

                  <div className="rounded-2xl border border-border/80 bg-surface-soft/40 p-4">
                    <span className="text-xs font-medium text-secondary">Service mode</span>
                    <p className="mt-1 font-semibold text-ink">
                      {booking.serviceMode === 'HOME_VISIT' ? 'Home Visit' : 'Repair Centre'}
                    </p>
                    <span className="text-xs text-secondary">
                      {booking.serviceMode === 'HOME_VISIT' ? 'Technician travels to you' : 'Drop-off at certified centre'}
                    </span>
                  </div>

                  <div className="rounded-2xl border border-border/80 bg-surface-soft/40 p-4">
                    <span className="text-xs font-medium text-secondary">Assigned technician</span>
                    <p className="mt-1 font-semibold text-ink">
                      {booking.assignedTechnician ? (
                        <span className="inline-flex items-center gap-1.5 text-teal">
                          <span>👤</span> {booking.assignedTechnician.displayName}
                        </span>
                      ) : (
                        <span className="text-secondary italic">Awaiting assignment</span>
                      )}
                    </p>
                    {booking.assignedTechnician?.serviceArea && (
                      <span className="text-xs text-secondary block mt-0.5">Area: {booking.assignedTechnician.serviceArea}</span>
                    )}
                  </div>
                </div>

                <div className="mt-4 rounded-2xl bg-surface-soft p-4">
                  <span className="text-xs font-bold uppercase tracking-wider text-secondary">Problem description</span>
                  <p className="mt-1 text-sm leading-relaxed text-ink">{booking.problemDescription}</p>
                </div>
              </article>
            )
          })}
        </div>
      )}
    </AccountLayout>
  )
}
