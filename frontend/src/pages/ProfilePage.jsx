import { useEffect, useState } from 'react'
import { getCustomerProfile, updateCustomerProfile } from '../api.js'
import AccountLayout from '../components/AccountLayout.jsx'

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState(false)
  useEffect(() => { let active = true; getCustomerProfile().then(value => { if (active) { setProfile(value); setName(value.fullName) } }).catch(e => { if (active) setError(e.message || 'Could not load your profile.') }); return () => { active = false } }, [])
  async function submit(event) { event.preventDefault(); setError(''); setNotice(''); if (name.trim().length < 2) { setError('Name must contain at least 2 characters.'); return } setBusy(true); try { const value = await updateCustomerProfile({ fullName: name }); setProfile(value); setName(value.fullName); setNotice('Your profile was updated.') } catch (e) { setError(e.message || 'Could not update your profile.') } finally { setBusy(false) } }
  return <AccountLayout><div className="mx-auto max-w-2xl"><p className="text-sm font-bold uppercase tracking-[.14em] text-teal">Your account</p><h1 className="mt-2 text-3xl font-bold">Profile</h1><p className="mt-2 text-secondary">Keep your account details up to date.</p>
    {!profile && !error && <p className="mt-8" role="status">Loading your profile…</p>}
    {error && !profile && <div role="alert" className="mt-8 rounded-2xl border border-border bg-white p-5 text-error">{error} <button onClick={() => window.location.reload()} className="ml-2 font-bold underline">Try again</button></div>}
    {profile && <form onSubmit={submit} className="mt-8 space-y-5 rounded-3xl border border-border bg-white p-6 shadow-card sm:p-8"><label className="block text-sm font-semibold">Full name<input value={name} maxLength="120" onChange={e=>setName(e.target.value)} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label><label className="block text-sm font-semibold">Email address<input value={profile.email} readOnly className="mt-2 min-h-12 w-full cursor-not-allowed rounded-xl border border-border bg-surface-soft px-4 text-secondary" /><span className="mt-1 block text-xs font-normal text-muted">Email changes are not available.</span></label><p className="text-sm text-secondary">Account type: {profile.role.toLowerCase()}</p>{notice && <p role="status" className="text-sm font-medium text-success">{notice}</p>}{error && <p role="alert" className="text-sm text-error">{error}</p>}<button disabled={busy} className="min-h-12 rounded-full bg-ink px-6 font-bold text-white disabled:opacity-60">{busy ? 'Saving…' : 'Save changes'}</button></form>}
  </div></AccountLayout>
}
