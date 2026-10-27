import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import {
  acceptTechnicianOffer,
  getSupplierProfile,
  getTechnicianAssignments,
  getTechnicianOffers,
  getTechnicianProfile,
  rejectTechnicianOffer,
} from '../api.js'
import AccountLayout from './AccountLayout.jsx'
import { roleHome } from './ProtectedRoute.jsx'

const loaders = { TECHNICIAN: getTechnicianProfile, SUPPLIER: getSupplierProfile }
const roleLabels = { TECHNICIAN: 'Technician', SUPPLIER: 'Supplier' }

function SummaryRow({ label, value }) {
  return (
    <div className="flex flex-col gap-1 border-b border-border py-4 last:border-0 sm:flex-row sm:items-start sm:justify-between">
      <dt className="text-sm font-medium text-secondary">{label}</dt>
      <dd className="text-sm font-semibold text-ink sm:max-w-[65%] sm:text-right">{value || 'Not configured'}</dd>
    </div>
  )
}

function SectionSkeleton() {
  return (
    <div className="animate-pulse space-y-3 py-4">
      <div className="h-20 rounded-2xl bg-surface-soft" />
      <div className="h-20 rounded-2xl bg-surface-soft" />
    </div>
  )
}

export default function ProviderPortalPage({ role, profile = false }) {
  const { identity } = useAuth()
  const [data, setData] = useState(null)
  const [state, setState] = useState('loading')
  const [error, setError] = useState('')

  // Technician offers & assignments state (used when role === 'TECHNICIAN' && !profile)
  const [offers, setOffers] = useState([])
  const [assignments, setAssignments] = useState([])
  const [offersLoading, setOffersLoading] = useState(false)
  const [actionError, setActionError] = useState('')
  const [actionNotice, setActionNotice] = useState('')
  const [actingOfferId, setActingOfferId] = useState(null)

  const loadTechnicianData = () => {
    if (role !== 'TECHNICIAN' || profile) return
    setOffersLoading(true)
    Promise.all([
      getTechnicianOffers().catch(() => []),
      getTechnicianAssignments().catch(() => []),
    ]).then(([offersData, assignmentsData]) => {
      setOffers(offersData)
      setAssignments(assignmentsData)
    }).finally(() => {
      setOffersLoading(false)
    })
  }

  useEffect(() => {
    const controller = new AbortController()
    setState('loading')
    loaders[role](controller.signal).then(result => {
      setData(result)
      setState('ready')
    }).catch(cause => {
      if (cause.name === 'AbortError') return
      setError(cause.message || 'We could not load your account details.')
      setState('error')
    })

    loadTechnicianData()

    return () => controller.abort()
  }, [role, profile])

  const handleAccept = async (offerId) => {
    setActingOfferId(offerId)
    setActionError('')
    setActionNotice('')
    try {
      await acceptTechnicianOffer(offerId)
      setActionNotice('Assignment accepted successfully! The repair booking is now assigned to you.')
      loadTechnicianData()
    } catch (err) {
      setActionError(err.message || 'Could not accept this offer. It may have expired or been reassigned.')
      loadTechnicianData()
    } finally {
      setActingOfferId(null)
    }
  }

  const handleReject = async (offerId) => {
    setActingOfferId(offerId)
    setActionError('')
    setActionNotice('')
    try {
      await rejectTechnicianOffer(offerId)
      setActionNotice('Offer declined. The system will attempt reassignment to the next eligible technician.')
      loadTechnicianData()
    } catch (err) {
      setActionError(err.message || 'Could not decline this offer.')
      loadTechnicianData()
    } finally {
      setActingOfferId(null)
    }
  }

  const name = data?.displayName || data?.businessName || identity?.fullName
  const heading = profile ? `${roleLabels[role]} profile` : `${roleLabels[role]} dashboard`

  return (
    <AccountLayout>
      <section className="mx-auto max-w-4xl">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-sm font-bold uppercase tracking-[.14em] text-teal">{roleLabels[role]} portal</p>
            <h1 className="mt-2 text-3xl font-bold tracking-tight sm:text-4xl">{heading}</h1>
            <p className="mt-2 text-secondary">
              {profile
                ? 'Review the account and capability information currently configured for you.'
                : `Welcome, ${name || 'provider'}. Manage your work and incoming assignment offers.`}
            </p>
          </div>
          <nav aria-label={`${roleLabels[role]} navigation`} className="flex gap-2 text-sm font-semibold">
            <Link className={`rounded-full border px-4 py-2 hover:bg-surface-soft ${!profile ? 'bg-surface-soft border-teal' : 'border-border'}`} to={roleHome(role)}>Dashboard</Link>
            <Link className={`rounded-full border px-4 py-2 hover:bg-surface-soft ${profile ? 'bg-surface-soft border-teal' : 'border-border'}`} to={role === 'TECHNICIAN' ? '/technician/profile' : '/supplier/profile'}>Profile</Link>
          </nav>
        </div>

        {state === 'loading' && (
          <p className="mt-8 rounded-2xl border border-border bg-white p-6 text-secondary" role="status">
            Loading your profile…
          </p>
        )}

        {state === 'error' && (
          <div className="mt-8 rounded-2xl border border-red-200 bg-red-50 p-6 text-error" role="alert">
            <h2 className="font-bold">Profile unavailable</h2>
            <p className="mt-2 text-sm">{error}</p>
          </div>
        )}

        {state === 'ready' && data && (
          <div className="mt-8 space-y-8">
            {actionNotice && (
              <div role="status" className="rounded-2xl border border-teal/30 bg-teal/5 p-4 text-sm text-ink shadow-sm flex items-center justify-between">
                <span>{actionNotice}</span>
                <button onClick={() => setActionNotice('')} className="ml-3 text-xs font-bold text-teal hover:underline">Dismiss</button>
              </div>
            )}

            {actionError && (
              <div role="alert" className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-error shadow-sm flex items-center justify-between">
                <span>{actionError}</span>
                <button onClick={() => setActionError('')} className="ml-3 text-xs font-bold text-error hover:underline">Dismiss</button>
              </div>
            )}

            {/* Technician Dashboard Specific Content */}
            {role === 'TECHNICIAN' && !profile && (
              <>
                {/* Pending Offers Section */}
                <section aria-labelledby="offers-heading" className="rounded-3xl border border-border bg-white p-6 shadow-card sm:p-8">
                  <div className="flex items-center justify-between border-b border-border pb-4">
                    <div>
                      <h2 id="offers-heading" className="text-xl font-bold text-ink">Pending Assignment Offers</h2>
                      <p className="text-xs text-secondary mt-1">Review incoming repair opportunities matched to your skills.</p>
                    </div>
                    <span className="rounded-full bg-accent/25 px-3.5 py-1 text-xs font-bold text-ink border border-accent/40">
                      {offers.length} pending
                    </span>
                  </div>

                  {offersLoading ? (
                    <div className="py-4">
                      <p className="sr-only" role="status">Loading offers…</p>
                      <SectionSkeleton />
                    </div>
                  ) : offers.length === 0 ? (
                    <div className="py-10 text-center">
                      <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-surface-soft text-xl text-secondary">
                        ⏱️
                      </div>
                      <p className="mt-3 text-sm font-semibold text-secondary">No pending assignment offers at this time.</p>
                      <p className="mx-auto mt-1 max-w-sm text-xs text-muted">When a customer books a repair matching your skills, availability, and capacity, an offer will appear here.</p>
                    </div>
                  ) : (
                    <div className="mt-6 space-y-5">
                      {offers.map(offer => {
                        const offerDate = offer.createdAt ? new Date(offer.createdAt).toLocaleString(undefined, {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        }) : null

                        return (
                          <div key={offer.id} className="rounded-2xl border border-border bg-surface-soft/50 p-5 transition-all hover:bg-surface-soft/80 sm:p-6">
                            <div className="flex flex-wrap items-start justify-between gap-3">
                              <div>
                                <div className="flex flex-wrap items-center gap-2">
                                  <span className="text-xs font-bold uppercase tracking-wider text-teal">
                                    {offer.gadget?.categoryName || 'Repair'}
                                  </span>
                                  <span className="rounded-md bg-ink/5 px-2 py-0.5 text-[11px] font-semibold text-secondary">
                                    {offer.serviceMode === 'HOME_VISIT' ? 'Home Visit' : 'Repair Centre'}
                                  </span>
                                  {offerDate && (
                                    <span className="text-[11px] text-muted">· {offerDate}</span>
                                  )}
                                </div>
                                <h3 className="mt-1.5 text-lg font-bold text-ink">{offer.service?.name}</h3>
                                <p className="mt-0.5 text-sm text-secondary">
                                  Target Device: <strong className="text-ink font-semibold">{offer.gadget?.brand} {offer.gadget?.model}</strong>
                                </p>
                              </div>

                              <div className="flex flex-wrap items-center gap-2">
                                {offer.matchScore != null && (
                                  <span className="inline-flex items-center gap-1 rounded-full border border-teal/30 bg-teal/10 px-3 py-1 text-xs font-bold text-teal">
                                    <span>⚡</span> {Number(offer.matchScore).toFixed(1)}% Match
                                  </span>
                                )}
                                <span className="rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-xs font-bold text-blue-800">
                                  Offer #{offer.id}
                                </span>
                              </div>
                            </div>

                            {/* Match Rationale / Ranking Explanation */}
                            {offer.matchRationale && (
                              <div className="mt-3.5 rounded-xl border border-teal/20 bg-teal/5 p-3 text-xs text-ink/90">
                                <div className="flex items-center gap-1.5 font-bold text-teal">
                                  <span>📊</span> Ranking & Match Breakdown:
                                </div>
                                <p className="mt-1 leading-relaxed text-secondary">{offer.matchRationale}</p>
                              </div>
                            )}

                            {/* Problem Description */}
                            <div className="mt-3 rounded-xl border border-border bg-white p-3.5 text-xs text-secondary">
                              <span className="block text-[10px] font-bold uppercase tracking-wider text-secondary">
                                Customer Problem Description
                              </span>
                              <p className="mt-1 text-sm leading-relaxed text-ink">{offer.problemDescription}</p>
                            </div>

                            {/* Actions */}
                            <div className="mt-5 flex flex-wrap gap-3 border-t border-border pt-4">
                              <button
                                disabled={actingOfferId === offer.id}
                                onClick={() => handleAccept(offer.id)}
                                className="inline-flex min-h-10 items-center justify-center rounded-full bg-accent px-6 text-xs font-bold text-ink hover:brightness-105 disabled:opacity-50 transition-all shadow-sm"
                              >
                                {actingOfferId === offer.id ? 'Accepting…' : 'Accept Assignment'}
                              </button>
                              <button
                                disabled={actingOfferId === offer.id}
                                onClick={() => handleReject(offer.id)}
                                className="inline-flex min-h-10 items-center justify-center rounded-full border border-border bg-white px-6 text-xs font-semibold text-error hover:bg-red-50 disabled:opacity-50 transition-all"
                              >
                                {actingOfferId === offer.id ? 'Declining…' : 'Decline Offer'}
                              </button>
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  )}
                </section>

                {/* Active Assignments Section */}
                <section aria-labelledby="assignments-heading" className="rounded-3xl border border-border bg-white p-6 shadow-card sm:p-8">
                  <div className="flex items-center justify-between border-b border-border pb-4">
                    <div>
                      <h2 id="assignments-heading" className="text-xl font-bold text-ink">Active Assigned Repairs</h2>
                      <p className="text-xs text-secondary mt-1">Bookings currently assigned to you for execution.</p>
                    </div>
                    <span className="rounded-full bg-teal/10 px-3.5 py-1 text-xs font-bold text-teal border border-teal/20">
                      {assignments.length} / {data.capacityLimit} active
                    </span>
                  </div>

                  {/* Workload Capacity Bar */}
                  {data?.capacityLimit && (
                    <div className="mt-4 rounded-xl border border-border/80 bg-surface-soft/60 p-4">
                      <div className="flex justify-between text-xs font-semibold">
                        <span className="text-secondary">Workload & Capacity Status</span>
                        <span className="text-ink">
                          {assignments.length} of {data.capacityLimit} slots filled ({Math.max(0, data.capacityLimit - assignments.length)} remaining)
                        </span>
                      </div>
                      <div className="mt-2.5 h-2 w-full overflow-hidden rounded-full bg-border">
                        <div
                          className="h-full bg-teal transition-all duration-300"
                          style={{ width: `${Math.min(100, (assignments.length / data.capacityLimit) * 100)}%` }}
                        />
                      </div>
                    </div>
                  )}

                  {assignments.length === 0 ? (
                    <div className="py-8 text-center text-sm text-secondary">
                      No active repair assignments. Accepted offers will appear here.
                    </div>
                  ) : (
                    <div className="mt-5 space-y-4">
                      {assignments.map(booking => (
                        <div key={booking.id} className="rounded-2xl border border-border bg-white p-5 shadow-sm">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <div>
                              <div className="flex items-center gap-2">
                                <span className="text-xs font-bold text-teal">Booking #{booking.id}</span>
                                <span className="text-[11px] rounded bg-ink/5 px-2 py-0.5 font-medium text-secondary">
                                  {booking.serviceMode === 'HOME_VISIT' ? 'Home Visit' : 'Repair Centre'}
                                </span>
                              </div>
                              <h3 className="mt-1 font-bold text-ink text-base">{booking.service?.name}</h3>
                              <p className="text-xs text-secondary mt-0.5">
                                Device: <strong className="text-ink">{booking.gadget?.brand} {booking.gadget?.model}</strong> ({booking.gadget?.categoryName})
                              </p>
                            </div>
                            <span className="rounded-full border border-teal/20 bg-teal/10 px-3 py-1 text-xs font-bold text-teal">
                              ASSIGNED
                            </span>
                          </div>
                          <div className="mt-3 rounded-xl bg-surface-soft p-3 text-xs text-secondary leading-relaxed">
                            <strong className="text-ink font-semibold">Customer problem:</strong> {booking.problemDescription}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </section>
              </>
            )}

            {/* Standard Profile & Capabilities Section */}
            <div className="grid gap-5 md:grid-cols-[1.2fr_.8fr]">
              <section className="rounded-3xl border border-border bg-white p-6 shadow-card sm:p-8" aria-labelledby="account-heading">
                <h2 id="account-heading" className="text-xl font-bold">Account details</h2>
                <dl className="mt-3">
                  <SummaryRow label="Name" value={name} />
                  <SummaryRow label="Sign-in email" value={data.email || identity?.email} />
                  {role === 'TECHNICIAN' ? (
                    <>
                      <SummaryRow label="Account status" value={data.active ? 'Active' : 'Inactive'} />
                      <SummaryRow label="Configured service area" value={data.serviceArea} />
                      <SummaryRow label="Availability" value={data.availability?.replaceAll('_', ' ').toLowerCase()} />
                      <SummaryRow label="Service modes" value={data.serviceModes?.length ? data.serviceModes.map(mode => mode.replaceAll('_', ' ').toLowerCase()).join(', ') : 'Not configured'} />
                      <SummaryRow label="Capacity limit" value={`${data.capacityLimit} concurrent assignments`} />
                    </>
                  ) : (
                    <>
                      <SummaryRow label="Business name" value={data.businessName} />
                      <SummaryRow label="Contact email" value={data.contactEmail || 'Not configured'} />
                      <SummaryRow label="Account status" value={data.active ? 'Active' : 'Inactive'} />
                    </>
                  )}
                </dl>
              </section>

              {role === 'TECHNICIAN' ? (
                <section className="rounded-3xl border border-border bg-white p-6 shadow-card sm:p-8" aria-labelledby="skills-heading">
                  <h2 id="skills-heading" className="text-xl font-bold">Configured repair skills</h2>
                  {data.skills?.length ? (
                    <ul className="mt-4 space-y-3">
                      {data.skills.map(skill => (
                        <li key={skill.id} className="rounded-xl bg-surface-soft px-4 py-3">
                          <p className="font-semibold">{skill.name}</p>
                          <p className="mt-1 text-sm text-secondary">{skill.categoryName}</p>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="mt-4 rounded-xl bg-surface-soft p-4 text-sm text-secondary">No repair skills are configured on this profile yet.</p>
                  )}
                  <p className="mt-5 text-xs leading-5 text-secondary">
                    Your skills and supported service modes determine which repair bookings are matched and offered to you.
                  </p>
                </section>
              ) : (
                <section className="rounded-3xl border border-border bg-white p-6 shadow-card sm:p-8" aria-labelledby="inventory-heading">
                  <h2 id="inventory-heading" className="text-xl font-bold">Inventory</h2>
                  <div className="mt-4 rounded-xl bg-surface-soft p-4">
                    <p className="font-semibold">Inventory is not enabled yet</p>
                    <p className="mt-2 text-sm leading-6 text-secondary">
                      Supplier profile access is ready. Inventory management is planned for a later stage, so no stock or order information is shown here.
                    </p>
                  </div>
                </section>
              )}
            </div>
          </div>
        )}
      </section>
    </AccountLayout>
  )
}
